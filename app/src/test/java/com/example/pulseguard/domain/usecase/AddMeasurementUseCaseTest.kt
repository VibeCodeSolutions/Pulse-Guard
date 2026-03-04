// com.example.pulseguard.domain.usecase.AddMeasurementUseCaseTest
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.fake.FakeBloodPressureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AddMeasurementUseCase].
 *
 * Verifies all validation rules (range checks, cross-field rule) against the
 * documented boundary values and ensures persistence is only called on valid input.
 *
 * Test naming convention: `methodName_condition_expectedResult`
 */
class AddMeasurementUseCaseTest {

    private lateinit var fakeRepo: FakeBloodPressureRepository
    private lateinit var useCase: AddMeasurementUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeBloodPressureRepository()
        useCase = AddMeasurementUseCase(fakeRepo)
    }

    // ── Happy path ────────────────────────────────────────────────────────

    @Test
    fun execute_allValuesValid_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_validInput_callsRepositoryInsertExactlyOnce() = runTest {
        useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.RIGHT,
            medicationTaken = true,
            timestamp = 1_000_000L,
        )
        assertEquals(1, fakeRepo.insertCallCount)
    }

    @Test
    fun execute_successResult_returnsNonZeroRowId() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        ) as AddMeasurementResult.Success
        assertTrue(result.rowId > 0)
    }

    // ── Systolic boundary values ──────────────────────────────────────────

    @Test
    fun execute_systolicAtMinBoundary_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MIN,
            diastolic = 30,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_systolicAtMaxBoundary_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MAX,
            diastolic = 30,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_systolicBelowMin_returnsSystolicOutOfRange() = runTest {
        val result = useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MIN - 1,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.SYSTOLIC_OUT_OF_RANGE, result)
    }

    @Test
    fun execute_systolicAboveMax_returnsSystolicOutOfRange() = runTest {
        val result = useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MAX + 1,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.SYSTOLIC_OUT_OF_RANGE, result)
    }

    @Test
    fun execute_systolicInvalid_doesNotCallRepository() = runTest {
        useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MIN - 1,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(0, fakeRepo.insertCallCount)
    }

    // ── Diastolic boundary values ─────────────────────────────────────────

    @Test
    fun execute_diastolicAtMinBoundary_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = AddMeasurementUseCase.DIASTOLIC_MIN,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_diastolicAtMaxBoundary_returnsSuccess() = runTest {
        // systolic must be strictly > diastolic: use SYSTOLIC_MAX
        val result = useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MAX,
            diastolic = AddMeasurementUseCase.DIASTOLIC_MAX,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_diastolicBelowMin_returnsDiastolicOutOfRange() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = AddMeasurementUseCase.DIASTOLIC_MIN - 1,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.DIASTOLIC_OUT_OF_RANGE, result)
    }

    @Test
    fun execute_diastolicAboveMax_returnsDiastolicOutOfRange() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = AddMeasurementUseCase.DIASTOLIC_MAX + 1,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.DIASTOLIC_OUT_OF_RANGE, result)
    }

    // ── Pulse boundary values ─────────────────────────────────────────────

    @Test
    fun execute_pulseAtMinBoundary_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = AddMeasurementUseCase.PULSE_MIN,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_pulseAtMaxBoundary_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = AddMeasurementUseCase.PULSE_MAX,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    @Test
    fun execute_pulseBelowMin_returnsPulseOutOfRange() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = AddMeasurementUseCase.PULSE_MIN - 1,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.PULSE_OUT_OF_RANGE, result)
    }

    @Test
    fun execute_pulseAboveMax_returnsPulseOutOfRange() = runTest {
        val result = useCase.execute(
            systolic = 120,
            diastolic = 80,
            pulse = AddMeasurementUseCase.PULSE_MAX + 1,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.PULSE_OUT_OF_RANGE, result)
    }

    // ── Cross-field rule: systolic > diastolic ────────────────────────────

    @Test
    fun execute_systolicEqualsDiastolic_returnsSystolicNotGreaterThanDiastolic() = runTest {
        val result = useCase.execute(
            systolic = 80,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.SYSTOLIC_NOT_GREATER_THAN_DIASTOLIC, result)
    }

    @Test
    fun execute_systolicLessThanDiastolic_returnsSystolicNotGreaterThanDiastolic() = runTest {
        val result = useCase.execute(
            systolic = 80,
            diastolic = 100,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.SYSTOLIC_NOT_GREATER_THAN_DIASTOLIC, result)
    }

    @Test
    fun execute_systolicOneAboveDiastolic_returnsSuccess() = runTest {
        val result = useCase.execute(
            systolic = 81,
            diastolic = 80,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertTrue(result is AddMeasurementResult.Success)
    }

    // ── Validation priority: systolic is checked before diastolic ─────────

    @Test
    fun execute_bothSystolicAndDiastolicInvalid_returnsSystolicOutOfRangeFirst() = runTest {
        val result = useCase.execute(
            systolic = AddMeasurementUseCase.SYSTOLIC_MIN - 1,
            diastolic = AddMeasurementUseCase.DIASTOLIC_MIN - 1,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(AddMeasurementResult.ValidationError.SYSTOLIC_OUT_OF_RANGE, result)
    }

    @Test
    fun execute_diastolicInvalid_doesNotCallRepository() = runTest {
        useCase.execute(
            systolic = 120,
            diastolic = AddMeasurementUseCase.DIASTOLIC_MIN - 1,
            pulse = 72,
            arm = MeasurementArm.LEFT,
            medicationTaken = false,
            timestamp = 1_000_000L,
        )
        assertEquals(0, fakeRepo.insertCallCount)
    }
}
