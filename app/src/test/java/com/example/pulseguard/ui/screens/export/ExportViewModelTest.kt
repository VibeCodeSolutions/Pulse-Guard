// com.example.pulseguard.ui.screens.export.ExportViewModelTest
package com.example.pulseguard.ui.screens.export

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.usecase.ExportToPdfUseCase
import com.example.pulseguard.fake.FakeBloodPressureRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ExportViewModel].
 *
 * [android.net.Uri] is handled via the Android stub layer
 * (`testOptions.unitTests.isReturnDefaultValues = true`), so [Uri.parse] returns
 * null. Tests check [Result.isSuccess] and state flags only — never the URI value.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExportViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeRepo: FakeBloodPressureRepository
    private lateinit var viewModel: ExportViewModel

    /** Convenience stub entry within any date range. */
    private val stubEntry = BloodPressureEntry(
        systolic = 120,
        diastolic = 80,
        pulse = 72,
        measurementArm = MeasurementArm.LEFT,
        medicationTaken = false,
        timestamp = 1_000L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBloodPressureRepository()
        // Inject testDispatcher as ioDispatcher so withContext(ioDispatcher) runs inline
        // on the TestCoroutineScheduler instead of a real IO thread pool.
        viewModel = ExportViewModel(ExportToPdfUseCase(fakeRepo, testDispatcher), fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────

    @Test
    fun initialState_dateRangeStartIsNull() {
        assertNull(viewModel.uiState.value.dateRangeStart)
    }

    @Test
    fun initialState_dateRangeEndIsNull() {
        assertNull(viewModel.uiState.value.dateRangeEnd)
    }

    @Test
    fun initialState_canGenerateIsFalse() {
        assertFalse(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun initialState_isDateRangeSelectedIsFalse() {
        assertFalse(viewModel.uiState.value.isDateRangeSelected)
    }

    // ── DateRangeSelected event ───────────────────────────────────────────

    @Test
    fun onEvent_dateRangeSelected_setsStartTime() = runTest {
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertEquals(500L, viewModel.uiState.value.dateRangeStart)
    }

    @Test
    fun onEvent_dateRangeSelected_setsEndTime() = runTest {
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertEquals(2_000L, viewModel.uiState.value.dateRangeEnd)
    }

    @Test
    fun onEvent_dateRangeSelected_isDateRangeSelectedIsTrue() = runTest {
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertTrue(viewModel.uiState.value.isDateRangeSelected)
    }

    @Test
    fun onEvent_dateRangeSelected_queriesEntryCountFromRepository() = runTest {
        fakeRepo.seedEntries(listOf(stubEntry)) // timestamp=1000, in range [500,2000]
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertEquals(1, viewModel.uiState.value.previewEntryCount)
    }

    @Test
    fun onEvent_dateRangeSelectedWithNoEntries_previewCountIsZero() = runTest {
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertEquals(0, viewModel.uiState.value.previewEntryCount)
    }

    @Test
    fun onEvent_dateRangeSelected_clearsPreviousPdfUri() = runTest {
        // Generate a PDF successfully so generatedPdfUri is set
        fakeRepo.generatePdfResult = Result.success(mockk<Uri>(relaxed = true))
        fakeRepo.seedEntries(listOf(stubEntry))
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        viewModel.onEvent(ExportEvent.GenerateClicked)
        // Change the date range — URI must be cleared
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 0L, endTime = 100L))
        assertNull(viewModel.uiState.value.generatedPdfUri)
    }

    // ── GenerateClicked event ─────────────────────────────────────────────

    @Test
    fun onEvent_generateClickedWithNoDateRange_doesNotSetIsGenerating() = runTest {
        viewModel.onEvent(ExportEvent.GenerateClicked)
        assertFalse(viewModel.uiState.value.isGenerating)
    }

    @Test
    fun onEvent_generateClickedWithValidRangeAndEntries_isGeneratingFalseAfterCompletion() = runTest {
        fakeRepo.generatePdfResult = Result.success(mockk<Uri>(relaxed = true))
        fakeRepo.seedEntries(listOf(stubEntry))
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        viewModel.onEvent(ExportEvent.GenerateClicked)
        assertFalse(viewModel.uiState.value.isGenerating)
    }

    @Test
    fun onEvent_generateClicked_repositoryFailure_setsErrorMessage() = runTest {
        fakeRepo.generatePdfResult = Result.failure(RuntimeException("Disk full"))
        fakeRepo.seedEntries(listOf(stubEntry))
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        viewModel.onEvent(ExportEvent.GenerateClicked)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onEvent_generateClicked_repositoryFailure_generatedPdfUriIsNull() = runTest {
        fakeRepo.generatePdfResult = Result.failure(RuntimeException("error"))
        fakeRepo.seedEntries(listOf(stubEntry))
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        viewModel.onEvent(ExportEvent.GenerateClicked)
        assertNull(viewModel.uiState.value.generatedPdfUri)
    }

    // ── ErrorDismissed event ──────────────────────────────────────────────

    @Test
    fun onEvent_errorDismissed_clearsErrorMessage() = runTest {
        fakeRepo.generatePdfResult = Result.failure(RuntimeException("error"))
        fakeRepo.seedEntries(listOf(stubEntry))
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        viewModel.onEvent(ExportEvent.GenerateClicked)
        viewModel.onEvent(ExportEvent.ErrorDismissed)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    // ── canGenerate computed property ─────────────────────────────────────

    @Test
    fun canGenerate_dateRangeSetAndEntriesExist_returnsTrue() = runTest {
        fakeRepo.seedEntries(listOf(stubEntry))
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertTrue(viewModel.uiState.value.canGenerate)
    }

    @Test
    fun canGenerate_dateRangeSetButNoEntries_returnsFalse() = runTest {
        viewModel.onEvent(ExportEvent.DateRangeSelected(startTime = 500L, endTime = 2_000L))
        assertFalse(viewModel.uiState.value.canGenerate)
    }
}
