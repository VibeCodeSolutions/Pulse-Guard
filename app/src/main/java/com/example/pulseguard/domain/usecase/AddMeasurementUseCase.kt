// com.example.pulseguard.domain.usecase.AddMeasurementUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.repository.BloodPressureRepository
import com.example.pulseguard.domain.model.MeasurementArm

/**
 * Typed result of [AddMeasurementUseCase.execute].
 *
 * Using a sealed interface makes every outcome explicit at the call site and
 * avoids string-based exception parsing in the presentation layer.
 */
sealed interface AddMeasurementResult {

    /** The measurement was persisted successfully. */
    data class Success(val rowId: Long) : AddMeasurementResult

    /**
     * A domain-level validation rule was violated.
     *
     * Each entry maps to exactly one invariant so the caller can convert it to
     * a user-visible string resource ID without any string parsing.
     */
    enum class ValidationError : AddMeasurementResult {
        SYSTOLIC_OUT_OF_RANGE,
        DIASTOLIC_OUT_OF_RANGE,
        PULSE_OUT_OF_RANGE,
        SYSTOLIC_NOT_GREATER_THAN_DIASTOLIC,
    }
}

/**
 * Use case for persisting a new blood pressure measurement.
 *
 * Validates all numeric inputs against the clinically allowed ranges and
 * enforces that systolic pressure is strictly greater than diastolic.
 * Delegates persistence to [BloodPressureRepository].
 *
 * @property repository The data source used to persist the entry.
 */
class AddMeasurementUseCase(private val repository: BloodPressureRepository) {

    /**
     * Validates the measurement data and, if valid, inserts it into the repository.
     *
     * Validation runs in declaration order; the first failing rule is returned
     * immediately. Cross-field validation (systolic > diastolic) is checked last.
     *
     * @param systolic        Systolic pressure in mmHg. Valid range: [SYSTOLIC_MIN]–[SYSTOLIC_MAX].
     * @param diastolic       Diastolic pressure in mmHg. Valid range: [DIASTOLIC_MIN]–[DIASTOLIC_MAX].
     * @param pulse           Heart rate in bpm. Valid range: [PULSE_MIN]–[PULSE_MAX].
     * @param arm             Which arm was used for the measurement.
     * @param medicationTaken Whether medication was taken prior to this measurement.
     * @param timestamp       Unix epoch millis representing when the measurement was taken.
     * @return [AddMeasurementResult.Success] with the new row ID on success, or the first
     *         [AddMeasurementResult.ValidationError] that was violated.
     */
    suspend fun execute(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        arm: MeasurementArm,
        medicationTaken: Boolean,
        timestamp: Long,
    ): AddMeasurementResult {
        if (systolic !in SYSTOLIC_MIN..SYSTOLIC_MAX) {
            return AddMeasurementResult.ValidationError.SYSTOLIC_OUT_OF_RANGE
        }
        if (diastolic !in DIASTOLIC_MIN..DIASTOLIC_MAX) {
            return AddMeasurementResult.ValidationError.DIASTOLIC_OUT_OF_RANGE
        }
        if (pulse !in PULSE_MIN..PULSE_MAX) {
            return AddMeasurementResult.ValidationError.PULSE_OUT_OF_RANGE
        }
        if (systolic <= diastolic) {
            return AddMeasurementResult.ValidationError.SYSTOLIC_NOT_GREATER_THAN_DIASTOLIC
        }

        val entry = BloodPressureEntry(
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            measurementArm = arm,
            medicationTaken = medicationTaken,
            timestamp = timestamp,
        )
        return AddMeasurementResult.Success(repository.insertEntry(entry))
    }

    companion object {
        /** Minimum allowed systolic value in mmHg. */
        const val SYSTOLIC_MIN = 60

        /** Maximum allowed systolic value in mmHg. */
        const val SYSTOLIC_MAX = 300

        /** Minimum allowed diastolic value in mmHg. */
        const val DIASTOLIC_MIN = 30

        /** Maximum allowed diastolic value in mmHg. */
        const val DIASTOLIC_MAX = 200

        /** Minimum allowed pulse value in bpm. */
        const val PULSE_MIN = 30

        /** Maximum allowed pulse value in bpm. */
        const val PULSE_MAX = 250
    }
}
