package com.codecraft.subvault.domain.repository

import com.codecraft.subvault.domain.model.PriceChangeLog
import com.codecraft.subvault.domain.model.SpendingSummary
import com.codecraft.subvault.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    fun getDashboardSummary(): Flow<SpendingSummary>
    fun getPriceHistory(subscriptionId: Long): Flow<List<PriceChangeLog>>
    suspend fun getSubscriptionById(id: Long): Subscription?
    suspend fun insertSubscription(subscription: Subscription)
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(subscription: Subscription)
}
