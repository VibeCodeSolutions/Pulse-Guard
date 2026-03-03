// com.example.pulseguard.data.repository.BloodPressureRepository
package com.example.pulseguard.data.repository

import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for blood pressure data.
 *
 * Abstracts the underlying data source (Room database) from the domain and UI layers.
 * All read operations expose reactive [Flow]s so consumers update automatically
 * when the database changes.
 *
 * The single implementation is [BloodPressureRepositoryImpl], wired via Koin.
 */
interface BloodPressureRepository {

    /**
     * Returns a [Flow] emitting all entries ordered by timestamp descending.
     */
    fun getAllEntries(): Flow<List<BloodPressureEntry>>

    /**
     * Returns a [Flow] of entries within the given timestamp range, ordered by
     * timestamp descending.
     *
     * @param startTime Start of the range (inclusive), Unix epoch millis.
     * @param endTime   End of the range (inclusive), Unix epoch millis.
     */
    fun getEntriesForDateRange(startTime: Long, endTime: Long): Flow<List<BloodPressureEntry>>

    /**
     * Returns a [Flow] of averaged values for the given timestamp range.
     * Emits `null` when no entries exist in the range.
     *
     * @param startTime Start of the range (inclusive), Unix epoch millis.
     * @param endTime   End of the range (inclusive), Unix epoch millis.
     */
    fun getAverageForDateRange(startTime: Long, endTime: Long): Flow<AggregatedValues?>

    /**
     * Returns a [Flow] of min/max values for the given timestamp range.
     * Emits `null` when no entries exist in the range.
     *
     * @param startTime Start of the range (inclusive), Unix epoch millis.
     * @param endTime   End of the range (inclusive), Unix epoch millis.
     */
    fun getMinMaxForDateRange(startTime: Long, endTime: Long): Flow<MinMaxValues?>

    /**
     * Returns a [Flow] emitting the total number of stored entries.
     * Emitting `0` indicates the empty state.
     */
    fun getEntryCount(): Flow<Int>

    /**
     * Inserts a new entry into the database.
     *
     * @param entry The entry to persist. The `id` field is auto-generated.
     * @return The database row ID of the inserted entry.
     */
    suspend fun insertEntry(entry: BloodPressureEntry): Long

    /**
     * Deletes the entry with the given [id].
     *
     * @param id Primary key of the entry to remove.
     */
    suspend fun deleteEntry(id: Long)
}
