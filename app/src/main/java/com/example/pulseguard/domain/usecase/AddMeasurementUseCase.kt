// com.example.pulseguard.domain.usecase.AddMeasurementUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.repository.BloodPressureRepository
import com.example.pulseguard.domain.model.MeasurementArm

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
     * @param systolic       Systolic pressure in mmHg. Valid range: [SYSTOLIC_MIN]–[SYSTOLIC_MAX].
     * @param diastolic      Diastolic pressure in mmHg. Valid range: [DIASTOLIC_MIN]–[DIASTOLIC_MAX].
     * @param pulse          Heart rate in bpm. Valid range: [PULSE_MIN]–[PULSE_MAX].
     * @param arm            Which arm was used for the measurement.
     * @param medicationTaken Whether medication was taken prior to this measurement.
     * @param timestamp      Unix epoch millis representing when the measurement was taken.
     * @return [Result.success] containing the new row ID on success,
     *         or [Result.failure] with an [IllegalArgumentException] describing
     *         the first validation violation.
     */
    suspend fun execute(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        arm: MeasurementArm,
        medicationTaken: Boolean,
        timestamp: Long,
    ): Result<Long> {
        val violations = mutableListOf<String>()

        if (systolic !in SYSTOLIC_MIN..SYSTOLIC_MAX) {
            violations += "systolic $systolic not in [$SYSTOLIC_MIN, $SYSTOLIC_MAX]"
        }
        if (diastolic !in DIASTOLIC_MIN..DIASTOLIC_MAX) {
            violations += "diastolic $diastolic not in [$DIASTOLIC_MIN, $DIASTOLIC_MAX]"
        }
        if (pulse !in PULSE_MIN..PULSE_MAX) {
            violations += "pulse $pulse not in [$PULSE_MIN, $PULSE_MAX]"
        }
        if (systolic <= diastolic) {
            violations += "systolic ($systolic) must be > diastolic ($diastolic)"
        }

        if (violations.isNotEmpty()) {
            return Result.failure(
                IllegalArgumentException("Validation failed: ${violations.joinToString("; ")}"),
            )
        }

        val entry = BloodPressureEntry(
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse,
            measurementArm = arm,
            medicationTaken = medicationTaken,
            timestamp = timestamp,
        )
        return Result.success(repository.insertEntry(entry))
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
