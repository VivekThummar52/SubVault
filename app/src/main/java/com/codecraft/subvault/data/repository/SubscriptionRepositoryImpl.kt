package com.codecraft.subvault.data.repository

import com.codecraft.subvault.data.local.ExchangeRateDao
import com.codecraft.subvault.data.local.PriceChangeDao
import com.codecraft.subvault.data.local.SubscriptionDao
import com.codecraft.subvault.domain.model.PriceChangeLog
import com.codecraft.subvault.domain.model.SpendingSummary
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.model.SubscriptionRenewal
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import com.codecraft.subvault.domain.util.CurrencyConverter
import com.codecraft.subvault.domain.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val dao: SubscriptionDao,
    private val priceChangeDao: PriceChangeDao,
    private val exchangeRateDao: ExchangeRateDao,
    private val preferenceRepository: PreferenceRepository,
    private val currencyConverter: CurrencyConverter
) : SubscriptionRepository {

    override fun getAllSubscriptions(): Flow<List<Subscription>> = 
        dao.getAllSubscriptions().onEach { checkAndInactivateSubscriptions(it) }

    override fun getActiveSubscriptions(): Flow<List<Subscription>> = 
        getAllSubscriptions().map { list -> list.filter { it.isActive && !isExpired(it) } }

    override fun getDashboardSummary(): Flow<SpendingSummary> {
        return combine(
            getActiveSubscriptions(),
            preferenceRepository.getUserPreferences(),
            exchangeRateDao.getRatesFlow()
        ) { subscriptions, prefs, rates ->
            val defaultCurrency = prefs.defaultCurrency
            val now = System.currentTimeMillis()
            val sevenDaysFromNow = now + (7 * 24 * 60 * 60 * 1000)

            val totalMonthlySpend = subscriptions.sumOf { sub ->
                val monthlyAmount = when (sub.cycle.lowercase()) {
                    "weekly" -> sub.amount * (52.0 / 12.0)
                    "yearly" -> sub.amount / 12.0
                    "lifetime" -> 0.0
                    else -> sub.amount
                }
                currencyConverter.convert(monthlyAmount, sub.currency, defaultCurrency)
            }

            val upcoming = subscriptions.map { sub ->
                SubscriptionRenewal(
                    id = sub.id,
                    name = sub.name,
                    nextRenewalDate = DateUtils.calculateNextRenewal(sub.renewalDate, sub.cycle),
                    amount = sub.amount,
                    currency = sub.currency
                )
            }.filter { 
                it.nextRenewalDate in now..sevenDaysFromNow 
            }.sortedBy { it.nextRenewalDate }

            SpendingSummary(
                totalMonthlySpend = totalMonthlySpend,
                activeCount = subscriptions.size,
                upcomingRenewals = upcoming,
                lastRatesUpdate = rates.maxOfOrNull { it.updatedAt }
            )
        }
    }

    override fun getPriceHistory(subscriptionId: Long): Flow<List<PriceChangeLog>> = 
        priceChangeDao.getPriceHistory(subscriptionId)

    override suspend fun getSubscriptionById(id: Long): Subscription? = dao.getSubscriptionById(id)

    override suspend fun insertSubscription(subscription: Subscription) = dao.insertSubscription(subscription)

    override suspend fun updateSubscription(subscription: Subscription) {
        val oldSubscription = dao.getSubscriptionById(subscription.id)
        if (oldSubscription != null && oldSubscription.amount != subscription.amount) {
            priceChangeDao.insertPriceLog(
                PriceChangeLog(
                    subscriptionId = subscription.id,
                    oldPrice = oldSubscription.amount,
                    newPrice = subscription.amount,
                    date = System.currentTimeMillis()
                )
            )
        }
        
        // Auto-reactivate if it was inactive but now has a future end date or no end date
        val now = System.currentTimeMillis()
        val shouldBeActive = subscription.endDate == null || subscription.endDate >= now
        val updatedSubscription = if (!subscription.isActive && shouldBeActive) {
            subscription.copy(isActive = true)
        } else {
            subscription
        }
        
        dao.updateSubscription(updatedSubscription)
    }

    override suspend fun deleteSubscription(subscription: Subscription) = dao.deleteSubscription(subscription)

    private suspend fun checkAndInactivateSubscriptions(subscriptions: List<Subscription>) {
        val now = System.currentTimeMillis()
        subscriptions.forEach { sub ->
            if (sub.isActive && sub.endDate != null && sub.endDate < now) {
                dao.updateSubscription(sub.copy(isActive = false))
            }
        }
    }

    private fun isExpired(subscription: Subscription): Boolean {
        return subscription.endDate != null && subscription.endDate < System.currentTimeMillis()
    }
}
