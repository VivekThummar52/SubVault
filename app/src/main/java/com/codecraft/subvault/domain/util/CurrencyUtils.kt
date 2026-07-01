package com.codecraft.subvault.domain.util

import java.util.*

object CurrencyUtils {
    fun getSymbol(currencyCode: String): String {
        if (currencyCode.isEmpty()) return "$"
        return try {
            Currency.getInstance(currencyCode).getSymbol(Locale.US)
        } catch (e: Exception) {
            "$"
        }
    }

    fun getProductShortName(name: String?): String {
        // 1. Sanitize and trim
        val sanitized = name?.replace(Regex("[^a-zA-Z0-9\\s]"), "")?.trim() 
            ?: return ""

        if (sanitized.isEmpty()) return ""

        // 2. Split into words
        val words = sanitized.split(Regex("\\s+"))
        
        // 3. Extract initials
        return words.take(2) // Take only the first two words
            .filter { it.isNotEmpty() }
            .map { it.take(1).uppercase() }
            .joinToString(separator = "")
    }
}
