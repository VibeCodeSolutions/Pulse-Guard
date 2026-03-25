// com.example.pulseguard.domain.repository.ReminderRepository
package com.example.pulseguard.domain.repository

import com.example.pulseguard.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for reminder data.
 */
interface ReminderRepository {

    /** Observes all reminders, ordered by time of day. */
    fun getAllReminders(): Flow<List<Reminder>>

    /** Returns all currently enabled reminders (non-reactive). */
    suspend fun getEnabledReminders(): List<Reminder>

    /** Returns the reminder with [id], or `null` if not found. */
    suspend fun getReminderById(id: Long): Reminder?

    /** Inserts or updates a reminder. Returns the row ID. */
    suspend fun insertReminder(reminder: Reminder): Long

    /** Deletes the reminder with the given [id]. */
    suspend fun deleteReminder(id: Long)

    /** Enables or disables the reminder with the given [id]. */
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
