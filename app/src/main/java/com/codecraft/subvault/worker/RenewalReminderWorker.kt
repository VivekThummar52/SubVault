package com.codecraft.subvault.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codecraft.subvault.data.local.SentNotificationDao
import com.codecraft.subvault.domain.model.SentNotification
import com.codecraft.subvault.domain.model.Subscription
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import com.codecraft.subvault.domain.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*

@HiltWorker
class RenewalReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SubscriptionRepository,
    private val sentNotificationDao: SentNotificationDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val subscriptions = repository.getActiveSubscriptions().first()
        val calendar = Calendar.getInstance()
        
        // Reset time to start of day for easier comparison
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        val targetIntervals = listOf(7, 3, 1, 0)

        for (sub in subscriptions) {
            val nextRenewal = DateUtils.calculateNextRenewal(sub.renewalDate, sub.cycle)
            
            // Normalize nextRenewal to start of day for accurate daysUntil calculation
            val nextRenewalCal = Calendar.getInstance().apply { 
                timeInMillis = nextRenewal
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val normalizedNextRenewal = nextRenewalCal.timeInMillis
            
            // Calculate days until renewal
            val diff = normalizedNextRenewal - todayStart
            val daysUntil = (diff / (1000 * 60 * 60 * 24)).toInt()

            if (daysUntil in targetIntervals) {
                // Check if already sent for this specific renewal day and interval
                val existing = sentNotificationDao.getSentNotification(sub.id, normalizedNextRenewal, daysUntil)
                if (existing == null) {
                    showSpecificNotification(sub, daysUntil)
                    sentNotificationDao.insertSentNotification(
                        SentNotification(
                            subscriptionId = sub.id,
                            renewalDate = normalizedNextRenewal,
                            intervalDays = daysUntil
                        )
                    )
                }
            }
        }
        
        // Cleanup old records (older than 30 days)
        sentNotificationDao.deleteOldNotifications(todayStart - (30L * 24 * 60 * 60 * 1000))

        return Result.success()
    }

    private fun showSpecificNotification(subscription: Subscription, daysUntil: Int) {
        val title = when (daysUntil) {
            0 -> "Renewal Today!"
            1 -> "Renewal Tomorrow"
            else -> "Upcoming Renewal"
        }
        val text = when (daysUntil) {
            0 -> "${subscription.name} renews today for ${subscription.amount} ${subscription.currency}"
            1 -> "${subscription.name} renews tomorrow"
            else -> "${subscription.name} renews in $daysUntil days"
        }
        
        val builder = NotificationCompat.Builder(applicationContext, "renewal_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            try {
                // Use subscription ID as notification ID to allow multiple reminders for different subs
                notify(subscription.id.toInt(), builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }

    private fun showNotification(count: Int) {
        // Keep for backward compatibility or group notifications if needed
    }
}
