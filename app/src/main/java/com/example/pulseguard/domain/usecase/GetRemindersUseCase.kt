// com.example.pulseguard.domain.usecase.GetRemindersUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes all stored reminders, ordered by time of day.
 */
class GetRemindersUseCase(
    private val repository: ReminderRepository,
) {

    /** Returns a reactive [Flow] of all reminders. */
    fun observe(): Flow<List<Reminder>> = repository.getAllReminders()
}
