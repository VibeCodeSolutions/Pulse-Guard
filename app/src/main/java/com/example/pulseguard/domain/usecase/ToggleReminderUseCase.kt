// com.example.pulseguard.domain.usecase.ToggleReminderUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.domain.repository.ReminderRepository
import com.example.pulseguard.notification.ReminderScheduler

/**
 * Toggles a reminder's enabled state and schedules or cancels its alarms
 * accordingly.
 */
class ToggleReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {

    /**
     * Sets the enabled flag for the reminder with [id] to [enabled].
     * Schedules alarms if enabling, cancels them if disabling.
     */
    suspend fun execute(id: Long, enabled: Boolean) {
        repository.setEnabled(id, enabled)
        val reminder = repository.getReminderById(id) ?: return
        if (enabled) {
            scheduler.schedule(reminder)
        } else {
            scheduler.cancel(reminder)
        }
    }
}
