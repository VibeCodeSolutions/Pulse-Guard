// com.example.pulseguard.ui.screens.dashboard.DashboardViewModelTest
package com.example.pulseguard.ui.screens.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.usecase.DeleteMeasurementUseCase
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import com.example.pulseguard.fake.FakeBloodPressureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [DashboardViewModel].
 *
 * ## Scheduler isolation
 * `midnightTicker = flowOf(Unit)` is injected into [GetDashboardDataUseCase] so the
 * production infinite-delay loop never enters the [kotlinx.coroutines.test.TestCoroutineScheduler].
 * This makes `runTest`'s epilogue `advanceUntilIdle()` safe without any manual scope
 * cancellation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fakeRepo: FakeBloodPressureRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBloodPressureRepository()
        // flowOf(Unit): emits once then completes — no infinite midnight delay in the scheduler
        viewModel = DashboardViewModel(
            getDashboardDataUseCase = GetDashboardDataUseCase(fakeRepo, midnightTicker = flowOf(Unit)),
            deleteMeasurementUseCase = DeleteMeasurementUseCase(fakeRepo),
            repository = fakeRepo,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun sampleEntry(id: Long = 1L, timestamp: Long = 1_000L) = BloodPressureEntry(
        id = id,
        systolic = 120,
        diastolic = 80,
        pulse = 70,
        measurementArm = MeasurementArm.LEFT,
        medicationTaken = false,
        timestamp = timestamp,
    )

    // ── Initial value (no subscriber — stateIn returns initialValue) ───────

    @Test
    fun initialValue_selectedPeriodIsWeek() {
        assertEquals(DashboardPeriod.WEEK, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun initialValue_isLoadingIsTrue() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun initialValue_pendingDeleteEntryIsNull() {
        assertNull(viewModel.uiState.value.pendingDeleteEntry)
    }

    // ── Period change ─────────────────────────────────────────────────────

    @Test
    fun onEvent_periodChangedToDay_uiStateReflectsDayPeriod() = runTest {
        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }

        viewModel.onEvent(DashboardEvent.PeriodChanged(DashboardPeriod.DAY))

        assertTrue(collected.any { it.selectedPeriod == DashboardPeriod.DAY })
    }

    @Test
    fun onEvent_periodChangedToMonth_uiStateReflectsMonthPeriod() = runTest {
        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }

        viewModel.onEvent(DashboardEvent.PeriodChanged(DashboardPeriod.MONTH))

        assertTrue(collected.any { it.selectedPeriod == DashboardPeriod.MONTH })
    }

    @Test
    fun onEvent_periodChangedTwice_lastPeriodIsReflected() = runTest {
        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }

        viewModel.onEvent(DashboardEvent.PeriodChanged(DashboardPeriod.DAY))
        viewModel.onEvent(DashboardEvent.PeriodChanged(DashboardPeriod.MONTH))

        assertTrue(collected.any { it.selectedPeriod == DashboardPeriod.MONTH })
    }

    // ── EntryDeleted ──────────────────────────────────────────────────────

    @Test
    fun onEvent_entryDeleted_setsNonNullPendingDeleteEntry() = runTest {
        val entry = sampleEntry()
        fakeRepo.seedEntries(listOf(entry))

        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }

        viewModel.onEvent(DashboardEvent.EntryDeleted(entry))

        assertTrue(collected.any { it.pendingDeleteEntry == entry })
    }

    @Test
    fun onEvent_entryDeleted_removesEntryFromRepository() = runTest {
        val entry = sampleEntry()
        fakeRepo.seedEntries(listOf(entry))

        viewModel.onEvent(DashboardEvent.EntryDeleted(entry))

        val remaining = fakeRepo.getAllEntries().first()
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun onEvent_entryDeleted_deleteCallCountIncremented() = runTest {
        val entry = sampleEntry()
        fakeRepo.seedEntries(listOf(entry))

        viewModel.onEvent(DashboardEvent.EntryDeleted(entry))

        assertEquals(1, fakeRepo.deleteCallCount)
    }

    // ── UndoDelete ────────────────────────────────────────────────────────

    @Test
    fun onEvent_undoDelete_clearsNullPendingDeleteEntry() = runTest {
        val entry = sampleEntry()
        fakeRepo.seedEntries(listOf(entry))

        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }

        viewModel.onEvent(DashboardEvent.EntryDeleted(entry))
        viewModel.onEvent(DashboardEvent.UndoDelete)

        assertTrue(collected.any { it.pendingDeleteEntry == null && !it.isLoading })
    }

    @Test
    fun onEvent_undoDelete_reInsertsEntryIntoRepository() = runTest {
        val entry = sampleEntry()
        fakeRepo.seedEntries(listOf(entry))

        viewModel.onEvent(DashboardEvent.EntryDeleted(entry))
        // Repo is empty after delete; undo should restore one entry.
        viewModel.onEvent(DashboardEvent.UndoDelete)

        val remaining = fakeRepo.getAllEntries().first()
        assertEquals(1, remaining.size)
    }

    // ── SnackbarDismissed ─────────────────────────────────────────────────

    @Test
    fun onEvent_snackbarDismissed_clearsPendingDeleteEntry() = runTest {
        val entry = sampleEntry()
        fakeRepo.seedEntries(listOf(entry))

        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }

        viewModel.onEvent(DashboardEvent.EntryDeleted(entry))
        viewModel.onEvent(DashboardEvent.SnackbarDismissed)

        assertTrue(collected.any { it.pendingDeleteEntry == null && !it.isLoading })
    }

    // ── Empty DB state after subscription ─────────────────────────────────

    @Test
    fun uiState_emptyRepositoryAfterSubscription_recentEntriesIsEmpty() = runTest {
        val collected = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { collected.add(it) }
        }
        // flowOf(Unit) + UnconfinedTestDispatcher: the non-loading state is emitted eagerly
        val nonLoadingState = collected.firstOrNull { !it.isLoading }
        assertNotNull("Expected a non-loading state to have been emitted", nonLoadingState)
        assertTrue(nonLoadingState!!.recentEntries.isEmpty())
    }
}
