// com.example.pulseguard.ui.screens.entry.EntryViewModel
package com.example.pulseguard.ui.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import com.example.pulseguard.ui.screens.entry.EntryUiState.Companion.FIELD_DIASTOLIC
import com.example.pulseguard.ui.screens.entry.EntryUiState.Companion.FIELD_PULSE
import com.example.pulseguard.ui.screens.entry.EntryUiState.Companion.FIELD_SYSTOLIC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Validation error messages (also mirrored in strings.xml for reference)
private const val ERROR_REQUIRED = "Dieses Feld ist erforderlich"
private const val ERROR_SYSTOLIC_RANGE =
    "Wert muss zwischen ${AddMeasurementUseCase.SYSTOLIC_MIN} " +
        "und ${AddMeasurementUseCase.SYSTOLIC_MAX} mmHg liegen"
private const val ERROR_DIASTOLIC_RANGE =
    "Wert muss zwischen ${AddMeasurementUseCase.DIASTOLIC_MIN} " +
        "und ${AddMeasurementUseCase.DIASTOLIC_MAX} mmHg liegen"
private const val ERROR_PULSE_RANGE =
    "Wert muss zwischen ${AddMeasurementUseCase.PULSE_MIN} " +
        "und ${AddMeasurementUseCase.PULSE_MAX} bpm liegen"
private const val ERROR_SYSTOLIC_GREATER = "Systolisch muss größer als Diastolisch sein"

private val ALL_FIELDS = setOf(FIELD_SYSTOLIC, FIELD_DIASTOLIC, FIELD_PULSE)

/**
 * ViewModel for the Entry screen.
 *
 * Owns the single [EntryUiState] stream and processes all [EntryEvent]s.
 * Validation runs on every field change so the UI always reflects the current
 * validity without computing anything itself (pure UDF). The [MutableStateFlow]
 * deduplicates identical states, avoiding unnecessary recompositions.
 *
 * @property addMeasurementUseCase Responsible for final validation and persistence.
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
        val errors = validate(
            current.systolic,
            current.diastolic,
            current.pulse,
            ALL_FIELDS,
        )
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors, touchedFields = ALL_FIELDS) }
            return
        }

        val systolic = current.systolic.toIntOrNull() ?: return
        val diastolic = current.diastolic.toIntOrNull() ?: return
        val pulse = current.pulse.toIntOrNull() ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val result = addMeasurementUseCase.execute(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                arm = current.measurementArm,
                medicationTaken = current.medicationTaken,
                timestamp = current.timestamp,
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saveSuccess = true) } },
                onFailure = { _uiState.update { it.copy(isSaving = false) } },
            )
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
     * This function is pure – it produces no side effects.
     */
    private fun validate(
        systolicStr: String,
        diastolicStr: String,
        pulseStr: String,
        touched: Set<String>,
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        val systolicInt = systolicStr.toIntOrNull()
        val diastolicInt = diastolicStr.toIntOrNull()
        val pulseInt = pulseStr.toIntOrNull()

        if (FIELD_SYSTOLIC in touched) {
            when {
                systolicStr.isEmpty() ->
                    errors[FIELD_SYSTOLIC] = ERROR_REQUIRED
                systolicInt == null
                    || systolicInt !in AddMeasurementUseCase.SYSTOLIC_MIN..AddMeasurementUseCase.SYSTOLIC_MAX ->
                    errors[FIELD_SYSTOLIC] = ERROR_SYSTOLIC_RANGE
            }
        }

        if (FIELD_DIASTOLIC in touched) {
            when {
                diastolicStr.isEmpty() ->
                    errors[FIELD_DIASTOLIC] = ERROR_REQUIRED
                diastolicInt == null
                    || diastolicInt !in AddMeasurementUseCase.DIASTOLIC_MIN..AddMeasurementUseCase.DIASTOLIC_MAX ->
                    errors[FIELD_DIASTOLIC] = ERROR_DIASTOLIC_RANGE
            }
        }

        if (FIELD_PULSE in touched) {
            when {
                pulseStr.isEmpty() ->
                    errors[FIELD_PULSE] = ERROR_REQUIRED
                pulseInt == null
                    || pulseInt !in AddMeasurementUseCase.PULSE_MIN..AddMeasurementUseCase.PULSE_MAX ->
                    errors[FIELD_PULSE] = ERROR_PULSE_RANGE
            }
        }

        // Cross-field: systolic must be strictly greater than diastolic.
        // Only evaluated when both fields are touched and individually valid.
        if (FIELD_SYSTOLIC in touched && FIELD_DIASTOLIC in touched
            && systolicInt != null && diastolicInt != null
            && FIELD_SYSTOLIC !in errors && FIELD_DIASTOLIC !in errors
            && systolicInt <= diastolicInt
        ) {
            errors[FIELD_SYSTOLIC] = ERROR_SYSTOLIC_GREATER
        }

        return errors
    }
}
