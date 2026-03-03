// com.example.pulseguard.ui.screens.entry.EntryViewModel
package com.example.pulseguard.ui.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseguard.R
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.usecase.AddMeasurementResult
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import com.example.pulseguard.ui.screens.entry.EntryUiState.Companion.FIELD_DIASTOLIC
import com.example.pulseguard.ui.screens.entry.EntryUiState.Companion.FIELD_PULSE
import com.example.pulseguard.ui.screens.entry.EntryUiState.Companion.FIELD_SYSTOLIC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val ALL_FIELDS = setOf(FIELD_SYSTOLIC, FIELD_DIASTOLIC, FIELD_PULSE)

/**
 * ViewModel for the Entry screen.
 *
 * Owns the single [EntryUiState] stream and processes all [EntryEvent]s.
 * Validation runs on every field change so the UI always reflects the current
 * validity without computing anything itself (pure UDF). The [MutableStateFlow]
 * deduplicates identical states, avoiding unnecessary recompositions.
 *
 * Validation errors are stored as [@androidx.annotation.StringRes] IDs in
 * [EntryUiState.validationErrors], keeping this class free of Android [android.content.Context]
 * while letting the UI resolve locale-correct strings via
 * [androidx.compose.ui.res.stringResource].
 *
 * @property addMeasurementUseCase Responsible for final domain validation and persistence.
 */
class EntryViewModel(
    private val addMeasurementUseCase: AddMeasurementUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())

    /** Publicly exposed, read-only state stream consumed by the UI. */
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    /**
     * Single entry point for all UI interactions.
     *
     * Dispatches the given [event] to the appropriate private handler.
     */
    fun onEvent(event: EntryEvent) {
        when (event) {
            is EntryEvent.SystolicChanged -> onSystolicChanged(event.value)
            is EntryEvent.DiastolicChanged -> onDiastolicChanged(event.value)
            is EntryEvent.PulseChanged -> onPulseChanged(event.value)
            is EntryEvent.ArmChanged -> _uiState.update { it.copy(measurementArm = event.arm) }
            EntryEvent.MedicationToggled -> _uiState.update { it.copy(medicationTaken = !it.medicationTaken) }
            is EntryEvent.TimestampChanged -> _uiState.update { it.copy(timestamp = event.timestamp) }
            EntryEvent.SaveClicked -> onSaveClicked()
        }
    }

    // ── Field change handlers ──────────────────────────────────────────────

    private fun onSystolicChanged(value: String) {
        _uiState.update { state ->
            val touched = state.touchedFields + FIELD_SYSTOLIC
            state.copy(
                systolic = value,
                touchedFields = touched,
                validationErrors = validate(value, state.diastolic, state.pulse, touched),
            )
        }
    }

    private fun onDiastolicChanged(value: String) {
        _uiState.update { state ->
            val touched = state.touchedFields + FIELD_DIASTOLIC
            state.copy(
                diastolic = value,
                touchedFields = touched,
                validationErrors = validate(state.systolic, value, state.pulse, touched),
            )
        }
    }

    private fun onPulseChanged(value: String) {
        _uiState.update { state ->
            val touched = state.touchedFields + FIELD_PULSE
            state.copy(
                pulse = value,
                touchedFields = touched,
                validationErrors = validate(state.systolic, state.diastolic, value, touched),
            )
        }
    }

    // ── Save handler ───────────────────────────────────────────────────────

    private fun onSaveClicked() {
        val current = _uiState.value

        // Force full validation with all fields marked as touched
        val errors = validate(current.systolic, current.diastolic, current.pulse, ALL_FIELDS)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors, touchedFields = ALL_FIELDS) }
            return
        }

        val systolic = current.systolic.toIntOrNull() ?: return
        val diastolic = current.diastolic.toIntOrNull() ?: return
        val pulse = current.pulse.toIntOrNull() ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            when (
                addMeasurementUseCase.execute(
                    systolic = systolic,
                    diastolic = diastolic,
                    pulse = pulse,
                    arm = current.measurementArm,
                    medicationTaken = current.medicationTaken,
                    timestamp = current.timestamp,
                )
            ) {
                is AddMeasurementResult.Success ->
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is AddMeasurementResult.ValidationError ->
                    // Domain guard triggered unexpectedly – UI validation should have caught this.
                    _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    // ── Validation ─────────────────────────────────────────────────────────

    /**
     * Validates all three numeric fields and returns the current error map.
     *
     * Errors are only generated for fields present in [touched], preventing
     * premature error display on fields the user has not yet interacted with.
     * Cross-field validation (systolic > diastolic) only runs when both fields
     * have been touched and individually pass their single-field checks.
     *
     * Map values are [@androidx.annotation.StringRes] resource IDs. The UI layer
     * resolves them to locale-correct strings via `stringResource()`, so this
     * function remains free of Android [android.content.Context].
     *
     * This function is pure – it produces no side effects.
     */
    private fun validate(
        systolicStr: String,
        diastolicStr: String,
        pulseStr: String,
        touched: Set<String>,
    ): Map<String, Int> {
        val errors = mutableMapOf<String, Int>()

        val systolicInt = systolicStr.toIntOrNull()
        val diastolicInt = diastolicStr.toIntOrNull()
        val pulseInt = pulseStr.toIntOrNull()

        if (FIELD_SYSTOLIC in touched) {
            when {
                systolicStr.isEmpty() ->
                    errors[FIELD_SYSTOLIC] = R.string.error_field_required
                systolicInt == null
                    || systolicInt !in AddMeasurementUseCase.SYSTOLIC_MIN..AddMeasurementUseCase.SYSTOLIC_MAX ->
                    errors[FIELD_SYSTOLIC] = R.string.error_systolic_range
            }
        }

        if (FIELD_DIASTOLIC in touched) {
            when {
                diastolicStr.isEmpty() ->
                    errors[FIELD_DIASTOLIC] = R.string.error_field_required
                diastolicInt == null
                    || diastolicInt !in AddMeasurementUseCase.DIASTOLIC_MIN..AddMeasurementUseCase.DIASTOLIC_MAX ->
                    errors[FIELD_DIASTOLIC] = R.string.error_diastolic_range
            }
        }

        if (FIELD_PULSE in touched) {
            when {
                pulseStr.isEmpty() ->
                    errors[FIELD_PULSE] = R.string.error_field_required
                pulseInt == null
                    || pulseInt !in AddMeasurementUseCase.PULSE_MIN..AddMeasurementUseCase.PULSE_MAX ->
                    errors[FIELD_PULSE] = R.string.error_pulse_range
            }
        }

        // Cross-field: systolic must be strictly greater than diastolic.
        // Only evaluated when both fields are touched and individually valid.
        if (FIELD_SYSTOLIC in touched && FIELD_DIASTOLIC in touched
            && systolicInt != null && diastolicInt != null
            && FIELD_SYSTOLIC !in errors && FIELD_DIASTOLIC !in errors
            && systolicInt <= diastolicInt
        ) {
            errors[FIELD_SYSTOLIC] = R.string.error_systolic_greater_diastolic
        }

        return errors
    }
}
