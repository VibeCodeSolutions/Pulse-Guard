// com.example.pulseguard.ui.screens.dashboard.DashboardEvent
package com.example.pulseguard.ui.screens.dashboard

import com.example.pulseguard.domain.model.DashboardPeriod

/**
 * User-driven events dispatched from the Dashboard screen to [DashboardViewModel].
 */
sealed interface DashboardEvent {

    /** The user selected a different aggregation period. */
    data class PeriodChanged(val period: DashboardPeriod) : DashboardEvent

    /** The user requested deletion of an entry. */
    data class EntryDeleted(val entryId: Long) : DashboardEvent
}
