package com.codecraft.subvault.domain.util

interface CurrencyConverter {
    /**
     * Converts an amount from one currency to another using a target currency.
     * In this basic implementation, we assume a base currency (like USD) for rates.
     */
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double
}

class FixedCurrencyConverter : CurrencyConverter {
    // Static exchange rates relative to 1 USD
    private val rates = mapOf(
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
        
        val fromRate = rates[fromCurrency] ?: 1.0
        val toRate = rates[toCurrency] ?: 1.0
        
        // Convert to USD base first, then to target
        val amountInUsd = amount / fromRate
        return amountInUsd * toRate
    }
}
