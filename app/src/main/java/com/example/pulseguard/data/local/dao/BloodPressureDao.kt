// com.example.pulseguard.data.local.dao.BloodPressureDao
package com.example.pulseguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for blood pressure measurement entries.
 *
 * All read operations return a [Flow] so the UI reacts automatically to
 * database changes (Room as Single Source of Truth). Write operations are
 * suspending functions, intended to be called from a coroutine scope.
 */
@Dao
interface BloodPressureDao {

    /**
     * Inserts a new blood pressure entry into the database.
     *
     * @param entry The entry to insert. The `id` field is auto-generated and ignored.
     * @return The row ID of the newly inserted entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: BloodPressureEntry): Long

    /**
     * Returns all blood pressure entries ordered by timestamp descending (most recent first).
     */
    @Query("SELECT * FROM blood_pressure_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<BloodPressureEntry>>

    /**
     * Returns entries within the given timestamp range, ordered by timestamp descending.
     *
     * @param startTime Start of the range (inclusive), Unix epoch millis.
     * @param endTime   End of the range (inclusive), Unix epoch millis.
     */
    @Query(
        """
        SELECT * FROM blood_pressure_entries
        WHERE timestamp BETWEEN :startTime AND :endTime
        ORDER BY timestamp DESC
        """,
    )
    fun getEntriesForDateRange(startTime: Long, endTime: Long): Flow<List<BloodPressureEntry>>

    /**
     * Returns the average systolic, diastolic, and pulse values for the given date range.
     * Emits `null` when no entries exist within the range.
     *
     * @param startTime Start of the range (inclusive), Unix epoch millis.
     * @param endTime   End of the range (inclusive), Unix epoch millis.
     */
    @Query(
        """
        SELECT
            AVG(systolic)  AS avgSystolic,
            AVG(diastolic) AS avgDiastolic,
            AVG(pulse)     AS avgPulse
        FROM blood_pressure_entries
        WHERE timestamp BETWEEN :startTime AND :endTime
        """,
    )
    fun getAverageForDateRange(startTime: Long, endTime: Long): Flow<AggregatedValues?>

    /**
     * Returns minimum and maximum systolic/diastolic values for the given date range.
     * Emits `null` when no entries exist within the range.
     *
     * @param startTime Start of the range (inclusive), Unix epoch millis.
     * @param endTime   End of the range (inclusive), Unix epoch millis.
     */
    @Query(
        """
        SELECT
            MIN(systolic)  AS minSystolic,
            MAX(systolic)  AS maxSystolic,
            MIN(diastolic) AS minDiastolic,
            MAX(diastolic) AS maxDiastolic
        FROM blood_pressure_entries
        WHERE timestamp BETWEEN :startTime AND :endTime
        """,
    )
    fun getMinMaxForDateRange(startTime: Long, endTime: Long): Flow<MinMaxValues?>

    /**
     * Deletes the entry with the given [id].
     *
     * Returns the number of rows deleted (0 if the id did not exist, 1 otherwise).
     * The `Int` return type is required because Room's KSP processor cannot resolve
     * the `V` (void / Unit) JVM signature in suspend @Query functions with Kotlin 2.x.
     *
     * @param id Primary key of the entry to delete.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM blood_pressure_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long): Int

    /**
     * Returns the total number of stored entries as a reactive [Flow].
     * Emitting `0` signals the empty state.
     */
    @Query("SELECT COUNT(*) FROM blood_pressure_entries")
    fun getEntryCount(): Flow<Int>
}
