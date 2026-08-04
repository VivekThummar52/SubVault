package com.codecraft.subvault.domain.util

interface CurrencyConverter {
    /**
     * Converts an amount from one currency to another using current rates.
     */
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double

    /**
     * Converts an amount using historical rates if possible.
     */
    suspend fun convertHistorical(amount: Double, fromCurrency: String, toCurrency: String, date: Long): Double
}
