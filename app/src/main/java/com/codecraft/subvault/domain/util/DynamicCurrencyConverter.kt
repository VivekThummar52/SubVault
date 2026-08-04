package com.codecraft.subvault.domain.util

import com.codecraft.subvault.data.local.ExchangeRateDao
import com.codecraft.subvault.data.remote.CurrencyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicCurrencyConverter @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val currencyApi: CurrencyApi
) : CurrencyConverter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val ratesCache = MutableStateFlow<Map<String, Double>>(emptyMap())

    init {
        scope.launch {
            exchangeRateDao.getRatesFlow().collect { rates ->
                if (rates.isNotEmpty()) {
                    ratesCache.value = rates.associate { it.code to it.rate }
                }
            }
        }
    }

    // Fallback rates relative to 1 USD in case database is empty
    private val fallbackRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "INR" to 83.0,
        "JPY" to 150.0,
        "CAD" to 1.35,
        "AUD" to 1.52,
        "BRL" to 4.97
    )

    override fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount

        val currentRates = ratesCache.value
        val rates = if (currentRates.isNotEmpty()) currentRates else fallbackRates
        
        val fromRate = rates[fromCurrency] ?: 1.0
        val toRate = rates[toCurrency] ?: 1.0

        val amountInUsd = amount / fromRate
        return amountInUsd * toRate
    }

    override suspend fun convertHistorical(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        date: Long
    ): Double {
        if (fromCurrency == toCurrency) return amount

        return try {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
            val response = currencyApi.getHistoricalRates(dateStr)
            val rates = response.rates + ("USD" to 1.0)
            
            val fromRate = rates[fromCurrency] ?: 1.0
            val toRate = rates[toCurrency] ?: 1.0
            
            (amount / fromRate) * toRate
        } catch (e: Exception) {
            // Fallback to latest rates if historical fetch fails
            convert(amount, fromCurrency, toCurrency)
        }
    }
}
