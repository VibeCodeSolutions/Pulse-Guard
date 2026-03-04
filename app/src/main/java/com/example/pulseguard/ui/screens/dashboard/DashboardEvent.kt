// com.example.pulseguard.ui.screens.dashboard.DashboardEvent
package com.example.pulseguard.ui.screens.dashboard

import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.DashboardPeriod

/**
 * User-driven events dispatched from the Dashboard screen to [DashboardViewModel].
 */
sealed interface DashboardEvent {

    /** The user selected a different aggregation period. */
    data class PeriodChanged(val period: DashboardPeriod) : DashboardEvent

    /**
     * The user swiped an entry to delete it.
     *
     * The full [entry] is passed (not just the ID) so the ViewModel can store it
     * for a potential undo re-insert without an additional DB round-trip.
     */
    data class EntryDeleted(val entry: BloodPressureEntry) : DashboardEvent

    /**
     * The user tapped the "Undo" action on the delete snackbar.
     * The ViewModel re-inserts the [DashboardUiState.pendingDeleteEntry].
     */
    data object UndoDelete : DashboardEvent

    /**
     * The delete snackbar was dismissed (timeout or swipe) without the undo action.
     * The ViewModel clears [DashboardUiState.pendingDeleteEntry].
     */
    data object SnackbarDismissed : DashboardEvent
}
