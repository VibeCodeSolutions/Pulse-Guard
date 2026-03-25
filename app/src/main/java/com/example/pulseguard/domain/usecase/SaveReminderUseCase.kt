// com.example.pulseguard.domain.usecase.SaveReminderUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.domain.repository.ReminderRepository
import com.example.pulseguard.notification.ReminderScheduler

/**
 * Saves a reminder to the database and schedules its alarms.
 */
class SaveReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {

    /**
     * Inserts or updates the given [reminder], then schedules the corresponding
     * exact alarms via [ReminderScheduler].
     *
     * @return The row ID of the inserted or updated reminder.
     */
    suspend fun execute(reminder: Reminder): Long {
        val id = repository.insertReminder(reminder)
        val saved = reminder.copy(id = id)
        if (saved.isEnabled) {
            scheduler.schedule(saved)
        }
        return id
    }
}
