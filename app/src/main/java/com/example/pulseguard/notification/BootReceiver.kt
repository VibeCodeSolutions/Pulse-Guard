// com.example.pulseguard.notification.BootReceiver
package com.example.pulseguard.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pulseguard.data.local.dao.ReminderDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get

/**
 * Reschedules all enabled reminder alarms after device reboot.
 *
 * AlarmManager alarms are lost on reboot, so this receiver restores them
 * by reading enabled reminders from Room and passing them to [ReminderScheduler].
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = get<ReminderDao>(ReminderDao::class.java)
                val scheduler = get<ReminderScheduler>(ReminderScheduler::class.java)
                val reminders = dao.getEnabledReminders()
                scheduler.rescheduleAll(reminders)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
