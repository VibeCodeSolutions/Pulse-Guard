// com.example.pulseguard.ui.screens.dashboard.DashboardViewModelTest
package com.example.pulseguard.ui.screens.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import com.example.pulseguard.fake.FakeBloodPressureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
            GetDashboardDataUseCase(fakeRepo, midnightTicker = flowOf(Unit))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial value (no subscriber — stateIn returns initialValue) ───────

    @Test
    fun initialValue_selectedPeriodIsWeek() {
        // Tests the stateIn initialValue before any subscriber activates the upstream
        assertEquals(DashboardPeriod.WEEK, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun initialValue_isLoadingIsTrue() {
        // Tests the stateIn initialValue before any subscriber activates the upstream
        assertTrue(viewModel.uiState.value.isLoading)
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

    // ── EntryDeleted (stub) ───────────────────────────────────────────────

    @Test
    fun onEvent_entryDeletedDispatched_doesNotThrow() {
        // EntryDeleted is a no-op stub; dispatching it must not throw
        viewModel.onEvent(DashboardEvent.EntryDeleted(entryId = 1L))
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
