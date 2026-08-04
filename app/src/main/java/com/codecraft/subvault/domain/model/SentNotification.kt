package com.codecraft.subvault.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sent_notifications")
data class SentNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val renewalDate: Long,
    val intervalDays: Int,
    val sentAt: Long = System.currentTimeMillis()
)
