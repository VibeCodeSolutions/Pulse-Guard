// com.example.pulseguard.domain.model.DashboardPeriod
package com.example.pulseguard.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Selectable time periods for the Dashboard screen.
 */
enum class DashboardPeriod {
    /** Today from midnight to now. */
    DAY,

    /** Last 7 days (6 days ago midnight to now). */
    WEEK,

    /** Last 30 days (29 days ago midnight to now). */
    MONTH,
}

/**
 * Computes the start and end of the period as Unix epoch milliseconds.
 *
 * Boundaries are anchored to UTC midnight so that stored timestamps
 * (which are UTC epoch millis) are always compared in the same coordinate system.
 *
 * @return Pair of (startMs, endMs).
 */
fun DashboardPeriod.toTimeRange(): Pair<Long, Long> {
    val todayUtc = LocalDate.now(ZoneOffset.UTC)
    val endMs = Instant.now().toEpochMilli()
    val startMs = when (this) {
        DashboardPeriod.DAY ->
            todayUtc.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        DashboardPeriod.WEEK ->
            todayUtc.minusDays(6).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        DashboardPeriod.MONTH ->
            todayUtc.minusDays(29).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    return startMs to endMs
}

/**
 * Returns a human-readable German label for this period.
 *
 * @param now The current [ZonedDateTime] (injected for testability).
 * @return A label such as "Heute" or "Letzte 7 Tage".
 */
@Suppress("UNUSED_PARAMETER")
fun DashboardPeriod.toPeriodLabel(now: ZonedDateTime = ZonedDateTime.now()): String = when (this) {
    DashboardPeriod.DAY -> "Heute"
    DashboardPeriod.WEEK -> "Letzte 7 Tage"
    DashboardPeriod.MONTH -> "Letzte 30 Tage"
}
