// com.example.pulseguard.fake.FakeBloodPressureRepository
package com.example.pulseguard.fake

import android.net.Uri
import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import com.example.pulseguard.domain.repository.BloodPressureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake implementation of [BloodPressureRepository] for unit tests.
 *
 * Uses [MutableStateFlow] so that all flow-based queries react synchronously
 * to [seedEntries] calls — no real database or Android framework needed.
 *
 * @property insertCallCount Tracks how many times [insertEntry] was called.
 * @property generatePdfResult Controls what [generatePdfUri] returns.
 */
class FakeBloodPressureRepository : BloodPressureRepository {

    private val _entries = MutableStateFlow<List<BloodPressureEntry>>(emptyList())

    /** Number of times [insertEntry] was invoked; use to assert persistence calls. */
    var insertCallCount: Int = 0

    /** Number of times [deleteEntry] was invoked; use to assert delete calls. */
    var deleteCallCount: Int = 0

    /**
     * Result returned by [generatePdfUri]. Defaults to failure so tests that
     * don't need PDF generation don't accidentally succeed.
     * Override per test with [Result.success] or [Result.failure].
     */
    var generatePdfResult: Result<Uri> = Result.failure(UnsupportedOperationException("Not configured"))

    // ── BloodPressureRepository implementation ────────────────────────────

    override fun getAllEntries(): Flow<List<BloodPressureEntry>> = _entries

    override fun getEntriesForDateRange(startTime: Long, endTime: Long): Flow<List<BloodPressureEntry>> =
        _entries.map { list -> list.filter { it.timestamp in startTime..endTime } }

    override fun getAverageForDateRange(startTime: Long, endTime: Long): Flow<AggregatedValues?> =
        _entries.map { list ->
            val filtered = list.filter { it.timestamp in startTime..endTime }
            if (filtered.isEmpty()) {
                null
            } else {
                AggregatedValues(
                    avgSystolic = filtered.map { it.systolic }.average().toFloat(),
                    avgDiastolic = filtered.map { it.diastolic }.average().toFloat(),
                    avgPulse = filtered.map { it.pulse }.average().toFloat(),
                )
            }
        }

    override fun getMinMaxForDateRange(startTime: Long, endTime: Long): Flow<MinMaxValues?> =
        _entries.map { list ->
            val filtered = list.filter { it.timestamp in startTime..endTime }
            if (filtered.isEmpty()) {
                null
            } else {
                MinMaxValues(
                    minSystolic = filtered.minOf { it.systolic },
                    maxSystolic = filtered.maxOf { it.systolic },
                    minDiastolic = filtered.minOf { it.diastolic },
                    maxDiastolic = filtered.maxOf { it.diastolic },
                )
            }
        }

    override fun getEntryCount(): Flow<Int> = _entries.map { it.size }

    override suspend fun insertEntry(entry: BloodPressureEntry): Long {
        insertCallCount++
        val newId = (_entries.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _entries.value = _entries.value + entry.copy(id = newId)
        return newId
    }

    override suspend fun deleteEntry(id: Long) {
        deleteCallCount++
        _entries.value = _entries.value.filter { it.id != id }
    }

    override suspend fun generatePdfUri(startTime: Long, endTime: Long): Result<Uri> =
        generatePdfResult

    // ── Test helpers ──────────────────────────────────────────────────────

    /** Replaces all stored entries with [entries] — reacts immediately on active flows. */
    fun seedEntries(entries: List<BloodPressureEntry>) {
        _entries.value = entries
    }

    /** Clears all stored entries, resets all counters, and restores the default PDF result. */
    fun reset() {
        _entries.value = emptyList()
        insertCallCount = 0
        deleteCallCount = 0
        generatePdfResult = Result.failure(UnsupportedOperationException("Not configured"))
    }
}
