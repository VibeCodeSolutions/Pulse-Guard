// com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import com.example.pulseguard.data.repository.BloodPressureRepository
import com.example.pulseguard.domain.model.BloodPressureCategory
import com.example.pulseguard.domain.model.ChartDataPoint
import com.example.pulseguard.domain.model.DashboardAggregation
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.domain.model.toPeriodLabel
import com.example.pulseguard.domain.model.toTimeRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Domain data combining aggregated stats and raw entries for the Dashboard screen.
 *
 * @property aggregation Aggregated stats for the period, or `null` when there are no entries.
 * @property entries     Raw entries in the period, sorted descending by timestamp.
 * @property chartData   Chart data points sorted ascending by timestamp.
 * @property period      The selected [DashboardPeriod].
 */
data class DashboardData(
    val aggregation: DashboardAggregation?,
    val entries: List<BloodPressureEntry>,
    val chartData: List<ChartDataPoint>,
    val period: DashboardPeriod,
)

/**
 * Use case that observes aggregated dashboard data for a given [DashboardPeriod].
 *
 * Combines three repository flows (entries, averages, min/max) into a single reactive
 * [DashboardData] stream that updates automatically whenever the database changes **or**
 * when UTC midnight crosses into the next day, keeping the time window always current
 * without restarting the entire subscription.
 *
 * Flow architecture:
 * ```
 * midnightTickerFlow()          — emits Unit at startup, then once per UTC midnight
 *   └─ flatMapLatest { _ ->
 *        val (startMs, endMs) = period.toTimeRange()   ← fresh on each tick
 *        combine(
 *          repo.getEntries(startMs, endMs),            ← Room Flow
 *          repo.getAverage(startMs, endMs),            ← Room Flow
 *          repo.getMinMax(startMs, endMs),             ← Room Flow
 *        ) { ... } → DashboardData
 *      }
 * ```
 *
 * @param repository The [BloodPressureRepository] injected via Koin.
 */
class GetDashboardDataUseCase(private val repository: BloodPressureRepository) {

    /**
     * Returns a [Flow] of [DashboardData] that re-emits whenever the underlying data
     * changes **or** at each UTC midnight (so the active time window stays accurate).
     *
     * @param period The time period to aggregate over.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(period: DashboardPeriod): Flow<DashboardData> =
        midnightTickerFlow()
            .flatMapLatest {
                val (startMs, endMs) = period.toTimeRange()
                combine(
                    repository.getEntriesForDateRange(startMs, endMs),
                    repository.getAverageForDateRange(startMs, endMs),
                    repository.getMinMaxForDateRange(startMs, endMs),
                ) { entries, avg, minMax ->
                    DashboardData(
                        aggregation = buildAggregation(avg, minMax, entries.size, period),
                        entries = entries,
                        chartData = buildChartData(entries),
                        period = period,
                    )
                }
            }

    /**
     * Emits [Unit] immediately, then suspends until the next UTC midnight before
     * emitting again. This keeps the active time window current without polling.
     */
    private fun midnightTickerFlow(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            val nowMs = Instant.now().toEpochMilli()
            val nextMidnightMs = LocalDate.now(ZoneOffset.UTC)
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            delay(maxOf(0L, nextMidnightMs - nowMs))
        }
    }

    private fun buildAggregation(
        avg: AggregatedValues?,
        minMax: MinMaxValues?,
        entryCount: Int,
        period: DashboardPeriod,
    ): DashboardAggregation? {
        if (avg == null || minMax == null) return null
        val category = BloodPressureCategory.fromValues(
            avg.avgSystolic.roundToInt(),
            avg.avgDiastolic.roundToInt(),
        )
        return DashboardAggregation(
            periodLabel = period.toPeriodLabel(ZonedDateTime.now()),
            avgSystolic = avg.avgSystolic,
            avgDiastolic = avg.avgDiastolic,
            avgPulse = avg.avgPulse,
            minSystolic = minMax.minSystolic,
            maxSystolic = minMax.maxSystolic,
            minDiastolic = minMax.minDiastolic,
            maxDiastolic = minMax.maxDiastolic,
            entryCount = entryCount,
            category = category,
        )
    }

    private fun buildChartData(entries: List<BloodPressureEntry>): List<ChartDataPoint> =
        entries
            .sortedBy { it.timestamp }
            .mapIndexed { index, entry ->
                ChartDataPoint(
                    x = index.toFloat(),
                    systolic = entry.systolic.toFloat(),
                    diastolic = entry.diastolic.toFloat(),
                    timestamp = entry.timestamp,
                )
            }
}
