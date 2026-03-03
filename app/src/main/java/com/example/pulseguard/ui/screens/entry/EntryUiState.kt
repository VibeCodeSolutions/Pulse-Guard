// com.example.pulseguard.ui.screens.entry.EntryUiState
package com.example.pulseguard.ui.screens.entry

import com.example.pulseguard.domain.model.MeasurementArm

/**
 * Immutable UI state for the Entry screen.
 *
 * All numeric values are held as [String] to support partial in-progress input
 * without coercion. Parsing to [Int] happens exclusively in [EntryViewModel]
 * upon save.
 *
 * @property systolic        Raw string typed for systolic pressure (mmHg).
 * @property diastolic       Raw string typed for diastolic pressure (mmHg).
 * @property pulse           Raw string typed for pulse / heart rate (bpm).
 * @property measurementArm  Currently selected measurement arm.
 * @property medicationTaken Whether the user reported medication as taken.
 * @property timestamp       Measurement time as Unix epoch millis; defaults to now.
 * @property validationErrors Map of field key → localised error message string.
 *                            Keys: [FIELD_SYSTOLIC], [FIELD_DIASTOLIC], [FIELD_PULSE].
 * @property touchedFields   Fields the user has interacted with; errors are only
 *                            displayed for touched fields (or after a save attempt).
 * @property isSaving        True while the repository insert is in flight.
 * @property saveSuccess     Becomes true after a successful save; triggers navigation
 *                            and haptic feedback side effects in the UI.
 */
data class EntryUiState(
    val systolic: String = "",
    val diastolic: String = "",
    val pulse: String = "",
    val measurementArm: MeasurementArm = MeasurementArm.LEFT,
    val medicationTaken: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val validationErrors: Map<String, String> = emptyMap(),
    val touchedFields: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
) {
    /**
     * Subset of [validationErrors] that should be displayed in the UI.
     * Only errors for fields in [touchedFields] are included, preventing
     * premature error display on pristine fields.
     */
    val visibleErrors: Map<String, String>
        get() = validationErrors.filter { (key, _) -> key in touchedFields }

    /**
     * True when all required fields are non-empty, have no validation errors,
     * and no save operation is currently in flight.
     */
    val isSaveEnabled: Boolean
        get() = systolic.isNotEmpty()
            && diastolic.isNotEmpty()
            && pulse.isNotEmpty()
            && validationErrors.isEmpty()
            && !isSaving

    companion object {
        /** Map key for the systolic field. */
        const val FIELD_SYSTOLIC = "systolic"

        /** Map key for the diastolic field. */
        const val FIELD_DIASTOLIC = "diastolic"

        /** Map key for the pulse field. */
        const val FIELD_PULSE = "pulse"
    }
}
