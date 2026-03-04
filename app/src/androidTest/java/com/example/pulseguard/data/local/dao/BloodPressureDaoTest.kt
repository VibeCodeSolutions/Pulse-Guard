// com.example.pulseguard.data.local.dao.BloodPressureDaoTest
package com.example.pulseguard.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pulseguard.data.local.PulseGuardDatabase
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.MeasurementArm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for [BloodPressureDao] using a Room in-memory database.
 *
 * Tests cover all 7 DAO operations (insert, getAll, getForDateRange, getAverage,
 * getMinMax, delete, getCount). Flow-based read methods are consumed with
 * `flow.first()` — no `Thread.sleep()` or fixed delays are used.
 *
 * The in-memory database is created fresh for each test in [setUp] and
 * closed in [tearDown], ensuring full test isolation.
 */
@RunWith(AndroidJUnit4::class)
class BloodPressureDaoTest {

    private lateinit var database: PulseGuardDatabase
    private lateinit var dao: BloodPressureDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PulseGuardDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        dao = database.bloodPressureDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ── insertEntry ───────────────────────────────────────────────────────

    @Test
    fun insertEntry_newEntry_returnsNonZeroRowId() = runTest {
        val rowId = dao.insertEntry(buildEntry(timestamp = 1_000L))
        assertTrue(rowId > 0)
    }

    @Test
    fun insertEntry_firstEntry_rowIdIsOne() = runTest {
        val rowId = dao.insertEntry(buildEntry(timestamp = 1_000L))
        assertEquals(1L, rowId)
    }

    // ── getAllEntries ─────────────────────────────────────────────────────

    @Test
    fun getAllEntries_emptyDatabase_returnsEmptyList() = runTest {
        val entries = dao.getAllEntries().first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun getAllEntries_afterOneInsert_returnsOneEntry() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80))
        val entries = dao.getAllEntries().first()
        assertEquals(1, entries.size)
    }

    @Test
    fun getAllEntries_afterOneInsert_entryHasCorrectSystolic() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 130, diastolic = 85))
        val entry = dao.getAllEntries().first().first()
        assertEquals(130, entry.systolic)
    }

    @Test
    fun getAllEntries_twoEntries_orderedByTimestampDescending() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80))
        dao.insertEntry(buildEntry(timestamp = 2_000L, systolic = 130, diastolic = 85))
        val entries = dao.getAllEntries().first()
        assertTrue(entries[0].timestamp > entries[1].timestamp)
    }

    // ── getEntriesForDateRange ────────────────────────────────────────────

    @Test
    fun getEntriesForDateRange_entryWithinRange_returnsEntry() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L))
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertEquals(1, entries.size)
    }

    @Test
    fun getEntriesForDateRange_entryBeforeRange_returnsEmpty() = runTest {
        dao.insertEntry(buildEntry(timestamp = 100L))
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun getEntriesForDateRange_entryAfterRange_returnsEmpty() = runTest {
        dao.insertEntry(buildEntry(timestamp = 5_000L))
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun getEntriesForDateRange_entryAtStartBoundary_returnsEntry() = runTest {
        dao.insertEntry(buildEntry(timestamp = 500L))
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertEquals(1, entries.size)
    }

    @Test
    fun getEntriesForDateRange_entryAtEndBoundary_returnsEntry() = runTest {
        dao.insertEntry(buildEntry(timestamp = 2_000L))
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertEquals(1, entries.size)
    }

    @Test
    fun getEntriesForDateRange_twoEntriesInRangeOneOut_returnsTwoEntries() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L))
        dao.insertEntry(buildEntry(timestamp = 1_500L))
        dao.insertEntry(buildEntry(timestamp = 5_000L)) // out of range
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertEquals(2, entries.size)
    }

    @Test
    fun getEntriesForDateRange_results_orderedByTimestampDescending() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L))
        dao.insertEntry(buildEntry(timestamp = 1_500L))
        val entries = dao.getEntriesForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertTrue(entries[0].timestamp > entries[1].timestamp)
    }

    // ── getAverageForDateRange ────────────────────────────────────────────

    @Test
    fun getAverageForDateRange_emptyRange_returnsNull() = runTest {
        val avg = dao.getAverageForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNull(avg)
    }

    @Test
    fun getAverageForDateRange_singleEntry_avgSystolicMatchesEntry() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80, pulse = 72))
        val avg = dao.getAverageForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(avg)
        assertEquals(120f, avg!!.avgSystolic)
    }

    @Test
    fun getAverageForDateRange_singleEntry_avgDiastolicMatchesEntry() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80, pulse = 72))
        val avg = dao.getAverageForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(avg)
        assertEquals(80f, avg!!.avgDiastolic)
    }

    @Test
    fun getAverageForDateRange_twoEntries_avgSystolicIsArithmetic() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80, pulse = 70))
        dao.insertEntry(buildEntry(timestamp = 1_500L, systolic = 140, diastolic = 90, pulse = 80))
        val avg = dao.getAverageForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(avg)
        assertEquals(130f, avg!!.avgSystolic)
    }

    @Test
    fun getAverageForDateRange_twoEntries_avgPulseIsArithmetic() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80, pulse = 70))
        dao.insertEntry(buildEntry(timestamp = 1_500L, systolic = 140, diastolic = 90, pulse = 80))
        val avg = dao.getAverageForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(avg)
        assertEquals(75f, avg!!.avgPulse)
    }

    // ── getMinMaxForDateRange ─────────────────────────────────────────────

    @Test
    fun getMinMaxForDateRange_emptyRange_returnsNull() = runTest {
        val minMax = dao.getMinMaxForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNull(minMax)
    }

    @Test
    fun getMinMaxForDateRange_singleEntry_minEqualsMaxSystolic() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80))
        val minMax = dao.getMinMaxForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(minMax)
        assertEquals(minMax!!.minSystolic, minMax.maxSystolic)
    }

    @Test
    fun getMinMaxForDateRange_twoEntries_minSystolicIsLower() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 110, diastolic = 75))
        dao.insertEntry(buildEntry(timestamp = 1_500L, systolic = 150, diastolic = 95))
        val minMax = dao.getMinMaxForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(minMax)
        assertEquals(110, minMax!!.minSystolic)
    }

    @Test
    fun getMinMaxForDateRange_twoEntries_maxSystolicIsHigher() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 110, diastolic = 75))
        dao.insertEntry(buildEntry(timestamp = 1_500L, systolic = 150, diastolic = 95))
        val minMax = dao.getMinMaxForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(minMax)
        assertEquals(150, minMax!!.maxSystolic)
    }

    @Test
    fun getMinMaxForDateRange_twoEntries_minDiastolicIsLower() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 110, diastolic = 75))
        dao.insertEntry(buildEntry(timestamp = 1_500L, systolic = 150, diastolic = 95))
        val minMax = dao.getMinMaxForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(minMax)
        assertEquals(75, minMax!!.minDiastolic)
    }

    @Test
    fun getMinMaxForDateRange_twoEntries_maxDiastolicIsHigher() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 110, diastolic = 75))
        dao.insertEntry(buildEntry(timestamp = 1_500L, systolic = 150, diastolic = 95))
        val minMax = dao.getMinMaxForDateRange(startTime = 500L, endTime = 2_000L).first()
        assertNotNull(minMax)
        assertEquals(95, minMax!!.maxDiastolic)
    }

    // ── deleteEntry ───────────────────────────────────────────────────────

    @Test
    fun deleteEntry_existingEntry_removesItFromGetAll() = runTest {
        val rowId = dao.insertEntry(buildEntry(timestamp = 1_000L))
        dao.deleteEntry(rowId)
        val entries = dao.getAllEntries().first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun deleteEntry_existingEntry_returnsOne() = runTest {
        val rowId = dao.insertEntry(buildEntry(timestamp = 1_000L))
        val deleted = dao.deleteEntry(rowId)
        assertEquals(1, deleted)
    }

    @Test
    fun deleteEntry_nonExistingId_returnsZero() = runTest {
        val deleted = dao.deleteEntry(id = 999L)
        assertEquals(0, deleted)
    }

    @Test
    fun deleteEntry_onlyTargetedEntryIsDeleted() = runTest {
        val rowId1 = dao.insertEntry(buildEntry(timestamp = 1_000L, systolic = 120, diastolic = 80))
        dao.insertEntry(buildEntry(timestamp = 2_000L, systolic = 130, diastolic = 85))
        dao.deleteEntry(rowId1)
        val entries = dao.getAllEntries().first()
        assertEquals(1, entries.size)
        assertEquals(130, entries.first().systolic)
    }

    // ── getEntryCount ─────────────────────────────────────────────────────

    @Test
    fun getEntryCount_emptyDatabase_returnsZero() = runTest {
        val count = dao.getEntryCount().first()
        assertEquals(0, count)
    }

    @Test
    fun getEntryCount_afterOneInsert_returnsOne() = runTest {
        dao.insertEntry(buildEntry(timestamp = 1_000L))
        val count = dao.getEntryCount().first()
        assertEquals(1, count)
    }

    @Test
    fun getEntryCount_afterThreeInserts_returnsThree() = runTest {
        repeat(3) { i -> dao.insertEntry(buildEntry(timestamp = i.toLong() + 1)) }
        val count = dao.getEntryCount().first()
        assertEquals(3, count)
    }

    @Test
    fun getEntryCount_afterInsertAndDelete_returnsZero() = runTest {
        val rowId = dao.insertEntry(buildEntry(timestamp = 1_000L))
        dao.deleteEntry(rowId)
        val count = dao.getEntryCount().first()
        assertEquals(0, count)
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun buildEntry(
        timestamp: Long,
        systolic: Int = 120,
        diastolic: Int = 80,
        pulse: Int = 72,
        arm: MeasurementArm = MeasurementArm.LEFT,
        medicationTaken: Boolean = false,
    ) = BloodPressureEntry(
        systolic = systolic,
        diastolic = diastolic,
        pulse = pulse,
        measurementArm = arm,
        medicationTaken = medicationTaken,
        timestamp = timestamp,
    )
}
