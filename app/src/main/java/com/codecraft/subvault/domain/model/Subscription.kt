package com.codecraft.subvault.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val amount: Double,
    val currency: String,
    val cycle: String,
    val renewalDate: Long,
    val endDate: Long? = null,
    val paymentMethod: String,
    val isActive: Boolean = true,
    val iconUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
