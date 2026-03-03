// com.example.pulseguard.ui.screens.entry.EntryEvent
package com.example.pulseguard.ui.screens.entry

import com.example.pulseguard.domain.model.MeasurementArm

/**
 * Sealed interface representing all UI interactions on the Entry screen.
 *
 * Following Unidirectional Data Flow, every user action is modelled as one
 * concrete event that the UI dispatches to [EntryViewModel.onEvent].
 * No business logic lives in the composables – they only send events.
 */
sealed interface EntryEvent {

    /** The user changed the raw text in the systolic pressure field. */
    data class SystolicChanged(val value: String) : EntryEvent

    /** The user changed the raw text in the diastolic pressure field. */
    data class DiastolicChanged(val value: String) : EntryEvent

    /** The user changed the raw text in the pulse / heart-rate field. */
    data class PulseChanged(val value: String) : EntryEvent

    /** The user selected a different measurement arm via the segmented button. */
    data class ArmChanged(val arm: MeasurementArm) : EntryEvent

    /** The user toggled the "medication taken" switch. */
    data object MedicationToggled : EntryEvent

    /** The user confirmed a new measurement timestamp via the date/time picker. */
    data class TimestampChanged(val timestamp: Long) : EntryEvent

    /** The user tapped the Save button (or pressed Done on the keyboard). */
    data object SaveClicked : EntryEvent
}
