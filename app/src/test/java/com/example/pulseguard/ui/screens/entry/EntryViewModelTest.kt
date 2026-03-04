// com.example.pulseguard.ui.screens.entry.EntryViewModelTest
package com.example.pulseguard.ui.screens.entry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import com.example.pulseguard.fake.FakeBloodPressureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [EntryViewModel].
 *
 * Uses [UnconfinedTestDispatcher] as the main dispatcher so that coroutines
 * launched in [androidx.lifecycle.ViewModel.viewModelScope] execute eagerly
 * and synchronously within `runTest` blocks — no manual advancing needed.
 *
 * Field keys ([EntryUiState.FIELD_SYSTOLIC] etc.) are used as map keys for
 * [EntryUiState.validationErrors]; tests verify key presence rather than
 * exact string-resource IDs to stay independent of resource compilation order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeRepo: FakeBloodPressureRepository
    private lateinit var viewModel: EntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBloodPressureRepository()
        viewModel = EntryViewModel(AddMeasurementUseCase(fakeRepo))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun initialState_systolicIsEmpty() {
        assertEquals("", viewModel.uiState.value.systolic)
    }

    @Test
    fun initialState_diastolicIsEmpty() {
        assertEquals("", viewModel.uiState.value.diastolic)
    }

    @Test
    fun initialState_pulseIsEmpty() {
        assertEquals("", viewModel.uiState.value.pulse)
    }

    @Test
    fun initialState_noValidationErrors() {
        assertTrue(viewModel.uiState.value.validationErrors.isEmpty())
    }

    @Test
    fun initialState_isSaveEnabledIsFalse() {
        assertFalse(viewModel.uiState.value.isSaveEnabled)
    }

    // ── Field change events ───────────────────────────────────────────────

    @Test
    fun onEvent_systolicChanged_updatesSystolicInState() {
        viewModel.onEvent(EntryEvent.SystolicChanged("120"))
        assertEquals("120", viewModel.uiState.value.systolic)
    }

    @Test
    fun onEvent_diastolicChanged_updatesDiastolicInState() {
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        assertEquals("80", viewModel.uiState.value.diastolic)
    }

    @Test
    fun onEvent_pulseChanged_updatesPulseInState() {
        viewModel.onEvent(EntryEvent.PulseChanged("72"))
        assertEquals("72", viewModel.uiState.value.pulse)
    }

    @Test
    fun onEvent_armChanged_updatesArmInState() {
        viewModel.onEvent(EntryEvent.ArmChanged(com.example.pulseguard.domain.model.MeasurementArm.RIGHT))
        assertEquals(
            com.example.pulseguard.domain.model.MeasurementArm.RIGHT,
            viewModel.uiState.value.measurementArm,
        )
    }

    @Test
    fun onEvent_medicationToggled_flipsMedicationTaken() {
        val initial = viewModel.uiState.value.medicationTaken
        viewModel.onEvent(EntryEvent.MedicationToggled)
        assertEquals(!initial, viewModel.uiState.value.medicationTaken)
    }

    @Test
    fun onEvent_medicationToggledTwice_restoresOriginalState() {
        val initial = viewModel.uiState.value.medicationTaken
        viewModel.onEvent(EntryEvent.MedicationToggled)
        viewModel.onEvent(EntryEvent.MedicationToggled)
        assertEquals(initial, viewModel.uiState.value.medicationTaken)
    }

    @Test
    fun onEvent_timestampChanged_updatesTimestampInState() {
        val newTimestamp = 999_999L
        viewModel.onEvent(EntryEvent.TimestampChanged(newTimestamp))
        assertEquals(newTimestamp, viewModel.uiState.value.timestamp)
    }

    // ── Validation – single field errors ─────────────────────────────────

    @Test
    fun onEvent_systolicBelowRange_setsErrorForSystolic() {
        viewModel.onEvent(EntryEvent.SystolicChanged("59"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_SYSTOLIC))
    }

    @Test
    fun onEvent_systolicAboveRange_setsErrorForSystolic() {
        viewModel.onEvent(EntryEvent.SystolicChanged("301"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_SYSTOLIC))
    }

    @Test
    fun onEvent_systolicEmpty_setsErrorForSystolic() {
        // Touch the field first with a valid value, then clear it
        viewModel.onEvent(EntryEvent.SystolicChanged("120"))
        viewModel.onEvent(EntryEvent.SystolicChanged(""))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_SYSTOLIC))
    }

    @Test
    fun onEvent_diastolicBelowRange_setsErrorForDiastolic() {
        viewModel.onEvent(EntryEvent.DiastolicChanged("29"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_DIASTOLIC))
    }

    @Test
    fun onEvent_diastolicAboveRange_setsErrorForDiastolic() {
        viewModel.onEvent(EntryEvent.DiastolicChanged("201"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_DIASTOLIC))
    }

    @Test
    fun onEvent_pulseBelowRange_setsErrorForPulse() {
        viewModel.onEvent(EntryEvent.PulseChanged("29"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_PULSE))
    }

    @Test
    fun onEvent_pulseAboveRange_setsErrorForPulse() {
        viewModel.onEvent(EntryEvent.PulseChanged("251"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_PULSE))
    }

    // ── Validation – cross-field rule ─────────────────────────────────────

    @Test
    fun onEvent_systolicEqualsAfterBothTouched_setsCrossFieldError() {
        viewModel.onEvent(EntryEvent.SystolicChanged("80"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_SYSTOLIC))
    }

    @Test
    fun onEvent_systolicLessThanDiastolicAfterBothTouched_setsCrossFieldError() {
        viewModel.onEvent(EntryEvent.SystolicChanged("80"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("100"))
        assertTrue(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_SYSTOLIC))
    }

    @Test
    fun onEvent_systolicGreaterThanDiastolic_noCrossFieldError() {
        viewModel.onEvent(EntryEvent.SystolicChanged("121"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        assertFalse(viewModel.uiState.value.validationErrors.containsKey(EntryUiState.FIELD_SYSTOLIC))
    }

    // ── Premature error display: untouched field errors not shown ─────────

    @Test
    fun onEvent_onlySystolicTouched_diastolicErrorNotVisible() {
        viewModel.onEvent(EntryEvent.SystolicChanged("120"))
        assertFalse(viewModel.uiState.value.visibleErrors.containsKey(EntryUiState.FIELD_DIASTOLIC))
    }

    // ── Save flow ─────────────────────────────────────────────────────────

    @Test
    fun onEvent_saveWithValidData_saveSuccessBecomesTrue() = runTest {
        viewModel.onEvent(EntryEvent.SystolicChanged("120"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        viewModel.onEvent(EntryEvent.PulseChanged("72"))
        viewModel.onEvent(EntryEvent.SaveClicked)
        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun onEvent_saveWithValidData_callsRepositoryInsert() = runTest {
        viewModel.onEvent(EntryEvent.SystolicChanged("120"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        viewModel.onEvent(EntryEvent.PulseChanged("72"))
        viewModel.onEvent(EntryEvent.SaveClicked)
        assertEquals(1, fakeRepo.insertCallCount)
    }

    @Test
    fun onEvent_saveWithEmptyFields_doesNotCallRepository() = runTest {
        viewModel.onEvent(EntryEvent.SaveClicked)
        assertEquals(0, fakeRepo.insertCallCount)
    }

    @Test
    fun onEvent_saveWithEmptyFields_setsErrorsForAllFields() = runTest {
        viewModel.onEvent(EntryEvent.SaveClicked)
        val errors = viewModel.uiState.value.validationErrors
        assertTrue(errors.containsKey(EntryUiState.FIELD_SYSTOLIC))
        assertTrue(errors.containsKey(EntryUiState.FIELD_DIASTOLIC))
        assertTrue(errors.containsKey(EntryUiState.FIELD_PULSE))
    }

    @Test
    fun onEvent_saveWithInvalidSystolic_saveSuccessStaysFalse() = runTest {
        viewModel.onEvent(EntryEvent.SystolicChanged("59"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        viewModel.onEvent(EntryEvent.PulseChanged("72"))
        viewModel.onEvent(EntryEvent.SaveClicked)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    // ── isSaveEnabled computed property ───────────────────────────────────

    @Test
    fun isSaveEnabled_allFieldsValidNoErrors_returnsTrue() {
        viewModel.onEvent(EntryEvent.SystolicChanged("120"))
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        viewModel.onEvent(EntryEvent.PulseChanged("72"))
        assertTrue(viewModel.uiState.value.isSaveEnabled)
    }

    @Test
    fun isSaveEnabled_withValidationError_returnsFalse() {
        viewModel.onEvent(EntryEvent.SystolicChanged("59")) // out of range
        viewModel.onEvent(EntryEvent.DiastolicChanged("80"))
        viewModel.onEvent(EntryEvent.PulseChanged("72"))
        assertFalse(viewModel.uiState.value.isSaveEnabled)
    }
}
