// com.example.pulseguard.notification.NotificationHelper
package com.example.pulseguard.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pulseguard.MainActivity
import com.example.pulseguard.R
import com.example.pulseguard.domain.model.ReminderType

/**
 * Helper for creating the notification channel and building reminder notifications.
 *
 * [createChannel] must be called once during [android.app.Application.onCreate] to
 * register the channel before any notification is posted.
 */
class NotificationHelper(private val context: Context) {

    /** Creates the reminder notification channel (idempotent). */
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Posts a notification for the given reminder.
     *
     * @param reminderId Unique reminder ID, also used as notification ID.
     * @param type       [ReminderType] to select the appropriate icon and title.
     * @param label      User-provided label shown as notification text.
     */
    fun showNotification(reminderId: Long, type: ReminderType, label: String) {
        val title = when (type) {
            ReminderType.MEDICATION ->
                context.getString(R.string.notification_title_medication)
            ReminderType.MEASUREMENT ->
                context.getString(R.string.notification_title_measurement)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val iconRes = when (type) {
            ReminderType.MEDICATION -> R.drawable.ic_notification_medication
            ReminderType.MEASUREMENT -> R.drawable.ic_notification_measurement
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(label)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(reminderId.toInt(), notification)
    }

    companion object {
        /** Notification channel ID for reminder notifications. */
        const val CHANNEL_ID = "pulse_guard_reminders"
    }
}
