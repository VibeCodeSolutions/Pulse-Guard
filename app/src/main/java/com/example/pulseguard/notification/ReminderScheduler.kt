// com.example.pulseguard.notification.ReminderScheduler
package com.example.pulseguard.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.data.local.entity.daysSet
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Schedules and cancels exact alarms for [Reminder] entities via [AlarmManager].
 *
 * Each reminder–day combination gets its own [PendingIntent] identified by a
 * unique request code: `reminderId * 10 + dayOfWeek.value`. This allows
 * independent scheduling and cancellation per day.
 *
 * When an alarm fires, [ReminderAlarmReceiver] shows the notification and
 * reschedules the alarm for the next occurrence (+7 days).
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    /**
     * Schedules alarms for all enabled days of the given [reminder].
     * Existing alarms for the same reminder are replaced.
     */
    fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled) return
        for (day in reminder.daysSet) {
            val triggerAt = nextTriggerMillis(day, reminder.hour, reminder.minute)
            val pi = buildPendingIntent(reminder, day)
            scheduleExact(triggerAt, pi)
        }
    }

    /**
     * Cancels all alarms associated with the given [reminder].
     */
    fun cancel(reminder: Reminder) {
        for (day in DayOfWeek.entries) {
            val pi = buildPendingIntent(reminder, day)
            alarmManager.cancel(pi)
        }
    }

    /**
     * Reschedules all alarms from a list of reminders.
     * Typically called by [BootReceiver] after device reboot.
     */
    fun rescheduleAll(reminders: List<Reminder>) {
        for (reminder in reminders) {
            schedule(reminder)
        }
    }

    /**
     * Schedules a single alarm for the next occurrence of [day] at
     * [hour]:[minute], exactly 7 days from now. Called by
     * [ReminderAlarmReceiver] after an alarm fires.
     */
    fun scheduleNextWeek(reminder: Reminder, day: DayOfWeek) {
        val now = System.currentTimeMillis()
        val triggerAt = now + SEVEN_DAYS_MILLIS
        val pi = buildPendingIntent(reminder, day)
        scheduleExact(triggerAt, pi)
    }

    /**
     * Schedules an exact alarm, falling back to an inexact alarm when the
     * `SCHEDULE_EXACT_ALARM` permission is not granted (Android 12+).
     */
    private fun scheduleExact(triggerAtMillis: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            // Fallback: inexact but still fires approximately on time.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pi,
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pi,
            )
        }
    }

    private fun buildPendingIntent(reminder: Reminder, day: DayOfWeek): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_TYPE, reminder.type.name)
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_LABEL, reminder.label)
            putExtra(ReminderAlarmReceiver.EXTRA_DAY_VALUE, day.value)
        }
        val requestCode = requestCode(reminder.id, day)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val SEVEN_DAYS_MILLIS = 7L * 24 * 60 * 60 * 1000

        /**
         * Computes the epoch millis for the next occurrence of [day] at
         * [hour]:[minute] in the system default time zone.
         * If today is [day] but the time has already passed, it advances to next week.
         */
        fun nextTriggerMillis(day: DayOfWeek, hour: Int, minute: Int): Long {
            val zone = ZoneId.systemDefault()
            val now = java.time.ZonedDateTime.now(zone)
            var target = LocalDate.now(zone)
                .with(TemporalAdjusters.nextOrSame(day))
                .atTime(LocalTime.of(hour, minute))
                .atZone(zone)
            if (!target.isAfter(now)) {
                target = target.plusWeeks(1)
            }
            return target.toInstant().toEpochMilli()
        }

        /** Unique request code per reminder–day combination. */
        fun requestCode(reminderId: Long, day: DayOfWeek): Int =
            reminderId.toInt() * 10 + day.value
    }
}
