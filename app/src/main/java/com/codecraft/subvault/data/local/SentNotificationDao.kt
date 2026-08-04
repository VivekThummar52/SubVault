package com.codecraft.subvault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.codecraft.subvault.domain.model.SentNotification

@Dao
interface SentNotificationDao {
    @Query("SELECT * FROM sent_notifications WHERE subscriptionId = :subscriptionId AND renewalDate = :renewalDate AND intervalDays = :intervalDays")
    suspend fun getSentNotification(subscriptionId: Long, renewalDate: Long, intervalDays: Int): SentNotification?

    @Insert
    suspend fun insertSentNotification(notification: SentNotification)

    @Query("DELETE FROM sent_notifications WHERE renewalDate < :expiryDate")
    suspend fun deleteOldNotifications(expiryDate: Long)
}
