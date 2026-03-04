// com.example.pulseguard.ui.screens.export.ExportEvent
package com.example.pulseguard.ui.screens.export

/**
 * UI events that can be dispatched from [ExportScreen] to [ExportViewModel].
 *
 * Follows the Unidirectional Data Flow (UDF) pattern: the screen never mutates
 * state directly but sends events to the ViewModel.
 */
sealed interface ExportEvent {

    /**
     * The user confirmed a date range selection.
     *
     * @param startTime Range start in epoch millis (inclusive).
     * @param endTime   Range end in epoch millis (inclusive).
     */
    data class DateRangeSelected(val startTime: Long, val endTime: Long) : ExportEvent

    /** The user tapped the "Generate PDF" button. */
    data object GenerateClicked : ExportEvent

    /** The user dismissed the error snackbar / dialog. */
    data object ErrorDismissed : ExportEvent
}
