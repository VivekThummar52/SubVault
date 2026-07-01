package com.codecraft.subvault.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codecraft.subvault.domain.repository.SubscriptionRepository
import com.codecraft.subvault.domain.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RenewalReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SubscriptionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val subscriptions = repository.getActiveSubscriptions().first()
        val upcoming = subscriptions.filter { 
            val nextRenewal = DateUtils.calculateNextRenewal(it.renewalDate, it.cycle)
            val daysUntil = (nextRenewal - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
            daysUntil in 0..1
        }

        if (upcoming.isNotEmpty()) {
            showNotification(upcoming.size)
        }

        return Result.success()
    }

    private fun showNotification(count: Int) {
        val builder = NotificationCompat.Builder(applicationContext, "renewal_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Subscription Renewal")
            .setContentText("You have $count subscriptions renewing soon!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            try {
                notify(1, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }
}
