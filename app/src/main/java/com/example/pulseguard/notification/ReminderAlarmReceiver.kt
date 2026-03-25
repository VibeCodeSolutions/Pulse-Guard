// com.example.pulseguard.notification.ReminderAlarmReceiver
package com.example.pulseguard.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.data.local.entity.encodeDays
import com.example.pulseguard.domain.model.ReminderType
import java.time.DayOfWeek
import org.koin.java.KoinJavaComponent.get

/**
 * BroadcastReceiver that fires when a scheduled reminder alarm triggers.
 *
 * Shows a notification via [NotificationHelper] and reschedules the alarm
 * for the same day next week via [ReminderScheduler].
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val typeName = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: return
        val label = intent.getStringExtra(EXTRA_REMINDER_LABEL) ?: return
        val dayValue = intent.getIntExtra(EXTRA_DAY_VALUE, -1)
        if (dayValue == -1) return

        val type = ReminderType.valueOf(typeName)
        val day = DayOfWeek.of(dayValue)

        // Show notification
        val notificationHelper = get<NotificationHelper>(NotificationHelper::class.java)
        notificationHelper.showNotification(reminderId, type, label)

        // Reschedule for next week (same day, same time)
        val scheduler = get<ReminderScheduler>(ReminderScheduler::class.java)
        val stub = Reminder(
            id = reminderId,
            label = label,
            type = type,
            hour = 0,
            minute = 0,
            days = encodeDays(setOf(day)),
        )
        scheduler.scheduleNextWeek(stub, day)
    }

    companion object {
        /** Intent extra key for the reminder's database ID. */
        const val EXTRA_REMINDER_ID = "extra_reminder_id"

        /** Intent extra key for the [ReminderType] name. */
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"

        /** Intent extra key for the reminder label. */
        const val EXTRA_REMINDER_LABEL = "extra_reminder_label"

        /** Intent extra key for the [DayOfWeek] value (1–7). */
        const val EXTRA_DAY_VALUE = "extra_day_value"
    }
}
