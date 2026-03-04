// com.example.pulseguard.domain.repository.BloodPressureRepository
package com.example.pulseguard.domain.repository

import android.net.Uri
import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for blood pressure data.
 */
interface BloodPressureRepository {

    fun getAllEntries(): Flow<List<BloodPressureEntry>>

    fun getEntriesForDateRange(startTime: Long, endTime: Long): Flow<List<BloodPressureEntry>>

    fun getAverageForDateRange(startTime: Long, endTime: Long): Flow<AggregatedValues?>

    fun getMinMaxForDateRange(startTime: Long, endTime: Long): Flow<MinMaxValues?>

    fun getEntryCount(): Flow<Int>

    suspend fun insertEntry(entry: BloodPressureEntry): Long

    suspend fun deleteEntry(id: Long)

    /**
     * Generates a PDF report for the given date range and returns its [Uri].
     */
    suspend fun generatePdfUri(startTime: Long, endTime: Long): Result<Uri>
}
