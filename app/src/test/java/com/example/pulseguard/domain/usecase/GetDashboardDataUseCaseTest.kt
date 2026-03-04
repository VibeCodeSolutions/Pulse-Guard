// com.example.pulseguard.domain.usecase.GetDashboardDataUseCaseTest
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.fake.FakeBloodPressureRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetDashboardDataUseCase].
 *
 * `midnightTicker = flowOf(Unit)` replaces the production infinite-delay ticker so that
 * `flow.first()` collects exactly one emission without leaving pending work in the scheduler.
 *
 * [fixedNow] pins the wall clock for all entry timestamps so the filter
 * `it.timestamp in startTime..endTime` is deterministic regardless of test execution speed.
 */
class GetDashboardDataUseCaseTest {

    private lateinit var fakeRepo: FakeBloodPressureRepository
    private lateinit var useCase: GetDashboardDataUseCase

    /** Fixed timestamp used for all entries — captured once per class instance, not per test. */
    private val fixedNow = System.currentTimeMillis()

    @Before
    fun setUp() {
        fakeRepo = FakeBloodPressureRepository()
        // flowOf(Unit): emits once then completes — no infinite midnight delay in the scheduler
        useCase = GetDashboardDataUseCase(fakeRepo, midnightTicker = flowOf(Unit))
    }

    // ── Empty database ────────────────────────────────────────────────────

    @Test
    fun observe_emptyRepository_returnsNullAggregation() = runTest {
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertNull(data.aggregation)
    }

    @Test
    fun observe_emptyRepository_returnsEmptyEntries() = runTest {
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertTrue(data.entries.isEmpty())
    }

    @Test
    fun observe_emptyRepository_returnsEmptyChartData() = runTest {
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertTrue(data.chartData.isEmpty())
    }

    @Test
    fun observe_emptyRepository_returnsPeriodInData() = runTest {
        val data = useCase.observe(DashboardPeriod.MONTH).first()
        assertEquals(DashboardPeriod.MONTH, data.period)
    }

    // ── Single entry ──────────────────────────────────────────────────────

    @Test
    fun observe_singleEntryInRange_returnsOneEntry() = runTest {
        fakeRepo.seedEntries(listOf(entryNow(systolic = 120, diastolic = 80, pulse = 72)))
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertEquals(1, data.entries.size)
    }

    @Test
    fun observe_singleEntryInRange_aggregationIsNotNull() = runTest {
        fakeRepo.seedEntries(listOf(entryNow(systolic = 120, diastolic = 80, pulse = 72)))
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertNotNull(data.aggregation)
    }

    @Test
    fun observe_singleEntryInRange_chartDataHasOnePoint() = runTest {
        fakeRepo.seedEntries(listOf(entryNow(systolic = 120, diastolic = 80, pulse = 72)))
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertEquals(1, data.chartData.size)
    }

    @Test
    fun observe_singleEntry_chartPointXIsZero() = runTest {
        fakeRepo.seedEntries(listOf(entryNow(systolic = 120, diastolic = 80, pulse = 72)))
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertEquals(0f, data.chartData.first().x)
    }

    @Test
    fun observe_singleEntry_chartPointSystolicMatchesEntry() = runTest {
        fakeRepo.seedEntries(listOf(entryNow(systolic = 135, diastolic = 85, pulse = 72)))
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertEquals(135f, data.chartData.first().systolic)
    }

    @Test
    fun observe_singleEntry_chartPointDiastolicMatchesEntry() = runTest {
        fakeRepo.seedEntries(listOf(entryNow(systolic = 135, diastolic = 85, pulse = 72)))
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertEquals(85f, data.chartData.first().diastolic)
    }

    // ── Multiple entries ──────────────────────────────────────────────────

    @Test
    fun observe_twoEntriesInRange_chartDataIsSortedByTimestampAscending() = runTest {
        // Both entries are within the last 30 days — timestamps differ by 1 second
        val older = entryAt(timestamp = fixedNow - 2_000L, systolic = 130, diastolic = 85, pulse = 70)
        val newer = entryAt(timestamp = fixedNow - 1_000L, systolic = 120, diastolic = 80, pulse = 72)
        fakeRepo.seedEntries(listOf(newer, older)) // seeded newest-first on purpose
        val data = useCase.observe(DashboardPeriod.MONTH).first()
        assertTrue(data.chartData[0].timestamp <= data.chartData[1].timestamp)
    }

    @Test
    fun observe_twoEntriesInRange_chartPointsHaveSequentialX() = runTest {
        val older = entryAt(timestamp = fixedNow - 2_000L, systolic = 130, diastolic = 85, pulse = 70)
        val newer = entryAt(timestamp = fixedNow - 1_000L, systolic = 120, diastolic = 80, pulse = 72)
        fakeRepo.seedEntries(listOf(newer, older))
        val data = useCase.observe(DashboardPeriod.MONTH).first()
        assertEquals(0f, data.chartData[0].x)
        assertEquals(1f, data.chartData[1].x)
    }

    // ── Period propagation ────────────────────────────────────────────────

    @Test
    fun observe_dayPeriod_returnsDayPeriodInData() = runTest {
        val data = useCase.observe(DashboardPeriod.DAY).first()
        assertEquals(DashboardPeriod.DAY, data.period)
    }

    @Test
    fun observe_weekPeriod_returnsWeekPeriodInData() = runTest {
        val data = useCase.observe(DashboardPeriod.WEEK).first()
        assertEquals(DashboardPeriod.WEEK, data.period)
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Creates an entry with a timestamp 1 second before [fixedNow] — always within any period. */
    private fun entryNow(systolic: Int, diastolic: Int, pulse: Int) =
        entryAt(fixedNow - 1_000L, systolic, diastolic, pulse)

    private fun entryAt(timestamp: Long, systolic: Int, diastolic: Int, pulse: Int) =
        BloodPressureEntry(
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            measurementArm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = timestamp,
        )
}
