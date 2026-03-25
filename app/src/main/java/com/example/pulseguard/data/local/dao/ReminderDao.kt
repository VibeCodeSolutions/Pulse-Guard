// com.example.pulseguard.data.local.dao.ReminderDao
package com.example.pulseguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pulseguard.data.local.entity.Reminder
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for [Reminder] entities.
 */
@Dao
interface ReminderDao {

    /**
     * Inserts or replaces a reminder. Returns the row ID of the inserted entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    /**
     * Returns all reminders ordered by time of day (hour, then minute).
     */
    @Query("SELECT * FROM reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    /**
     * Returns all currently enabled reminders. Non-reactive — used by
     * [com.example.pulseguard.notification.BootReceiver] to reschedule alarms.
     */
    @Query("SELECT * FROM reminders WHERE is_enabled = 1")
    suspend fun getEnabledReminders(): List<Reminder>

    /**
     * Returns a single reminder by [id], or `null` if it doesn't exist.
     */
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    /**
     * Deletes the reminder with the given [id]. Returns the number of rows deleted.
     */
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long): Int

    /**
     * Sets the [enabled] flag for the reminder with the given [id].
     *
     * Returns the number of rows updated. The `Int` return type is required because
     * Room's KSP processor cannot resolve the `V` (void / Unit) JVM signature in
     * suspend `@Query` functions with Kotlin 2.x.
     */
    @Query("UPDATE reminders SET is_enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean): Int
}
