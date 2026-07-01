package com.codecraft.subvault.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.repository.PreferenceRepository
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import com.codecraft.subvault.domain.util.CurrencyConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import java.util.*

@Immutable
data class AnalyticsState(
    val categoryData: List<CategoryData> = emptyList(),
    val totalSpending: Double = 0.0,
    val averageMonthlySpending: Double = 0.0,
    val yearlySpending: Double = 0.0,
    val insights: List<String> = emptyList(),
    val trendPoints: List<Float> = emptyList(),
    val defaultCurrency: String = "USD",
    val isYearlyView: Boolean = false
)

@Immutable
data class CategoryData(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val color: Color
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val preferenceRepository: PreferenceRepository,
    private val currencyConverter: CurrencyConverter
) : ViewModel() {

    private val categoryColors = listOf(
        Color(0xFF6750A4), Color(0xFF958DA5), Color(0xFFD0BCFF),
        Color(0xFF625B71), Color(0xFF7D5260), Color(0xFFB3261E),
        Color(0xFFF2B8B5), Color(0xFFF9DEDC), Color(0xFF31111D)
    )

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear = _selectedYear.asStateFlow()

    private val _trendRange = MutableStateFlow(6) // 6 or 12 months
    val trendRange = _trendRange.asStateFlow()

    val uiState: StateFlow<AnalyticsState> = combine(
        repository.getAllSubscriptions(),
        preferenceRepository.getUserPreferences(),
        _selectedMonth,
        _selectedYear,
        _trendRange
    ) { subscriptions, prefs, month, year, range ->
        val defaultCurrency = prefs.defaultCurrency
        val isYearly = month == 12

        // Filter subscriptions active during the selected period
        val filteredSubscriptions = subscriptions.filter { sub ->
            isSubscriptionActiveInPeriod(sub, month, year)
        }

        val total = filteredSubscriptions.sumOf { sub ->
            val amount = if (isYearly) {
                calculateYearlyPriceForPeriod(sub, year)
            } else {
                calculateMonthlyPriceForPeriod(sub, month, year)
            }
            currencyConverter.convert(amount, sub.currency, defaultCurrency)
        }

        val categories = filteredSubscriptions.groupBy { it.category }.toList()
            .mapIndexed { index, (category, subs) ->
                val categoryTotal = subs.sumOf { sub ->
                    val amount = if (isYearly) {
                        calculateYearlyPriceForPeriod(sub, year)
                    } else {
                        calculateMonthlyPriceForPeriod(sub, month, year)
                    }
                    currencyConverter.convert(amount, sub.currency, defaultCurrency)
                }
                CategoryData(
                    category = category,
                    totalAmount = categoryTotal,
                    percentage = if (total > 0) (categoryTotal / total).toFloat() else 0f,
                    color = categoryColors[index % categoryColors.size]
                )
            }.sortedByDescending { it.totalAmount }

        // Calculate trend points
        val trend = calculateTrend(subscriptions, defaultCurrency, range)

        val insights = generateInsights(filteredSubscriptions, subscriptions, categories, month, year, defaultCurrency)

        AnalyticsState(
            categoryData = categories,
            totalSpending = total,
            averageMonthlySpending = if (isYearly) total / 12.0 else total,
            yearlySpending = if (isYearly) total else total * 12.0,
            insights = insights,
            trendPoints = trend,
            defaultCurrency = defaultCurrency,
            isYearlyView = isYearly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    fun selectMonth(month: Int) { _selectedMonth.value = month }
    fun selectYear(year: Int) { _selectedYear.value = year }
    fun setTrendRange(months: Int) { _trendRange.value = months }

    private fun isSubscriptionActiveInPeriod(sub: Subscription, month: Int, year: Int): Boolean {
        val startCal = Calendar.getInstance().apply { timeInMillis = sub.renewalDate }
        val periodStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            if (month == 12) {
                set(Calendar.MONTH, 0)
            } else {
                set(Calendar.MONTH, month)
            }
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val periodEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            if (month == 12) {
                set(Calendar.MONTH, 11)
                set(Calendar.DAY_OF_MONTH, 31)
            } else {
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        // Active if started before or during period AND (no end date or ended after or during period)
        val startedBeforeEnd = sub.renewalDate <= periodEnd.timeInMillis
        val notEndedBeforeStart = sub.endDate == null || sub.endDate >= periodStart.timeInMillis
        
        return startedBeforeEnd && notEndedBeforeStart
    }

    private fun calculateMonthlyPriceForPeriod(subscription: Subscription, month: Int, year: Int): Double {
        if (subscription.cycle.lowercase() == "lifetime") {
            val startCal = Calendar.getInstance().apply { timeInMillis = subscription.renewalDate }
            return if (startCal.get(Calendar.MONTH) == month && startCal.get(Calendar.YEAR) == year) {
                subscription.amount
            } else {
                0.0
            }
        }
        return when (subscription.cycle.lowercase()) {
            "yearly" -> subscription.amount / 12.0
            "weekly" -> subscription.amount * (52.0 / 12.0)
            else -> subscription.amount
        }
    }

    private fun calculateYearlyPriceForPeriod(subscription: Subscription, year: Int): Double {
        if (subscription.cycle.lowercase() == "lifetime") {
            val startCal = Calendar.getInstance().apply { timeInMillis = subscription.renewalDate }
            return if (startCal.get(Calendar.YEAR) == year) {
                subscription.amount
            } else {
                0.0
            }
        }
        return when (subscription.cycle.lowercase()) {
            "yearly" -> subscription.amount
            "weekly" -> subscription.amount * 52.0
            "monthly" -> subscription.amount * 12.0
            else -> subscription.amount * 12.0
        }
    }

    private fun calculateTrend(subscriptions: List<Subscription>, defaultCurrency: String, months: Int): List<Float> {
        return (0 until months).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -(months - 1 - i))
            val m = cal.get(Calendar.MONTH)
            val y = cal.get(Calendar.YEAR)
            
            subscriptions.filter { isSubscriptionActiveInPeriod(it, m, y) }
                .sumOf { sub ->
                    currencyConverter.convert(calculateMonthlyPriceForPeriod(sub, m, y), sub.currency, defaultCurrency)
                }.toFloat()
        }
    }

    private fun generateInsights(
        filtered: List<Subscription>,
        all: List<Subscription>,
        categories: List<CategoryData>,
        month: Int,
        year: Int,
        defaultCurrency: String
    ): List<String> {
        val insights = mutableListOf<String>()
        
        if (categories.isNotEmpty()) {
            insights.add("You spend most on ${categories.first().category}.")
        }

        // Historical Comparison (Current vs Previous Month)
        if (month != 12) {
            val prevMonth = if (month == 0) 11 else month - 1
            val prevYear = if (month == 0) year - 1 else year
            
            val currentTotal = filtered.sumOf { currencyConverter.convert(calculateMonthlyPriceForPeriod(it, month, year), it.currency, defaultCurrency) }
            val prevTotal = all.filter { isSubscriptionActiveInPeriod(it, prevMonth, prevYear) }
                .sumOf { currencyConverter.convert(calculateMonthlyPriceForPeriod(it, prevMonth, prevYear), it.currency, defaultCurrency) }
            
            if (prevTotal > 0) {
                val diff = currentTotal - prevTotal
                if (diff > 0) {
                    insights.add("Your monthly subscriptions increased by ${String.format("%.2f", diff)} $defaultCurrency.")
                } else if (diff < 0) {
                    insights.add("Your monthly subscriptions decreased by ${String.format("%.2f", -diff)} $defaultCurrency.")
                }
            }
        }

        return insights
    }
}
