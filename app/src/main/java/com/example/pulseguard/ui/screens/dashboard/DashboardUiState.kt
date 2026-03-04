// com.example.pulseguard.ui.screens.dashboard.DashboardUiState
package com.example.pulseguard.ui.screens.dashboard

import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.ChartDataPoint
import com.example.pulseguard.domain.model.DashboardAggregation
import com.example.pulseguard.domain.model.DashboardPeriod

/**
 * Immutable UI state for the Dashboard screen.
 *
 * @property selectedPeriod The currently selected aggregation period.
 * @property aggregation    Aggregated stats for the period, or `null` when there are no entries.
 * @property recentEntries  Entries for the period, sorted newest first.
 * @property chartData      Chart data points sorted oldest first (for the trend line).
 * @property isLoading      Whether the initial data load is still in progress.
 */
data class DashboardUiState(
    val selectedPeriod: DashboardPeriod = DashboardPeriod.WEEK,
    val aggregation: DashboardAggregation? = null,
    val recentEntries: List<BloodPressureEntry> = emptyList(),
    val chartData: List<ChartDataPoint> = emptyList(),
    val isLoading: Boolean = true,
) {
    /** `true` when the screen has finished loading but has no entries to display. */
    val isEmpty: Boolean get() = !isLoading && recentEntries.isEmpty()
}
