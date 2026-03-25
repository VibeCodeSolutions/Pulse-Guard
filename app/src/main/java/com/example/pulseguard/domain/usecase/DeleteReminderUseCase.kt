// com.example.pulseguard.domain.usecase.DeleteReminderUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.domain.repository.ReminderRepository
import com.example.pulseguard.notification.ReminderScheduler

/**
 * Deletes a reminder from the database and cancels its scheduled alarms.
 */
class DeleteReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {

    /**
     * Cancels alarms and removes the reminder with [id] from the database.
     * If the reminder does not exist, this is a no-op.
     */
    suspend fun execute(id: Long) {
        val reminder = repository.getReminderById(id) ?: return
        scheduler.cancel(reminder)
        repository.deleteReminder(id)
    }
}
