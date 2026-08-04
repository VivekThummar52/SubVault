package com.codecraft.subvault.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey val code: String, // e.g., "EUR", "INR"
    val rate: Double,             // Relative to USD
    val updatedAt: Long = System.currentTimeMillis()
)
