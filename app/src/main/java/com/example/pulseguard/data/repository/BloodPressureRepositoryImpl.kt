// com.example.pulseguard.data.repository.BloodPressureRepositoryImpl
package com.example.pulseguard.data.repository

import com.example.pulseguard.data.local.dao.BloodPressureDao
import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [BloodPressureRepository].
 *
 * Delegates all persistence operations to [BloodPressureDao]. This class is the
 * sole point of access to the local Room database and acts as the Single Source
 * of Truth for blood pressure data throughout the application.
 *
 * @param dao The Room DAO for blood pressure entries, injected via Koin.
 */
class BloodPressureRepositoryImpl(
    private val dao: BloodPressureDao,
) : BloodPressureRepository {

    override fun getAllEntries(): Flow<List<BloodPressureEntry>> =
        dao.getAllEntries()

    override fun getEntriesForDateRange(
        startTime: Long,
        endTime: Long,
    ): Flow<List<BloodPressureEntry>> =
        dao.getEntriesForDateRange(startTime, endTime)

    override fun getAverageForDateRange(
        startTime: Long,
        endTime: Long,
    ): Flow<AggregatedValues?> =
        dao.getAverageForDateRange(startTime, endTime)

    override fun getMinMaxForDateRange(
        startTime: Long,
        endTime: Long,
    ): Flow<MinMaxValues?> =
        dao.getMinMaxForDateRange(startTime, endTime)

    override fun getEntryCount(): Flow<Int> =
        dao.getEntryCount()

    override suspend fun insertEntry(entry: BloodPressureEntry): Long =
        dao.insertEntry(entry)

    override suspend fun deleteEntry(id: Long) {
        dao.deleteEntry(id) // Int (affected rows) is intentionally discarded
    }
}
