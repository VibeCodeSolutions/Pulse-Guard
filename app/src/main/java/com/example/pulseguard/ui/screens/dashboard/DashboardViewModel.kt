// com.example.pulseguard.ui.screens.dashboard.DashboardViewModel
package com.example.pulseguard.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.domain.repository.BloodPressureRepository
import com.example.pulseguard.domain.usecase.DeleteMeasurementUseCase
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 *
 * ### State composition
 * Two independent [MutableStateFlow]s are combined into the single [uiState]:
 * - [_selectedPeriod] drives a [flatMapLatest] over [GetDashboardDataUseCase], switching
 *   the active Room query whenever the period changes.
 * - [_pendingDeleteEntry] holds the most recently deleted entry, enabling undo. Its
 *   presence (`non-null`) signals the UI to display the undo snackbar.
 *
 * ### Delete & Undo
 * On [DashboardEvent.EntryDeleted] the entry is stored in [_pendingDeleteEntry] and
 * immediately deleted from the database. If the user taps "Undo" (→ [DashboardEvent.UndoDelete])
 * before the snackbar times out, the entry is re-inserted with `id = 0` so Room
 * assigns a fresh primary key. Dismissing the snackbar ([DashboardEvent.SnackbarDismissed])
 * simply clears [_pendingDeleteEntry]; the database row is already gone.
 *
 * @param getDashboardDataUseCase Provides reactive dashboard data per period.
 * @param deleteMeasurementUseCase Deletes a single entry by id.
 * @param repository Direct repository access for undo re-insertion (no validation needed —
 *                   the entry was already valid at initial insert time).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val deleteMeasurementUseCase: DeleteMeasurementUseCase,
    private val repository: BloodPressureRepository,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(DashboardPeriod.WEEK)
    private val _pendingDeleteEntry = MutableStateFlow<BloodPressureEntry?>(null)

    /**
     * Reactive UI state that updates whenever the selected period, the underlying
     * database data, or the pending-delete entry changes.
     */
    val uiState = combine(
        _selectedPeriod.flatMapLatest { period ->
            getDashboardDataUseCase.observe(period).map { data -> period to data }
        },
        _pendingDeleteEntry,
    ) { (period, data), pendingDelete ->
        DashboardUiState(
            selectedPeriod = period,
            aggregation = data.aggregation,
            recentEntries = data.entries,
            chartData = data.chartData,
            isLoading = false,
            pendingDeleteEntry = pendingDelete,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    /**
     * Dispatches a [DashboardEvent] to the appropriate handler.
     *
     * @param event The event emitted by the UI.
     */
    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.PeriodChanged -> _selectedPeriod.value = event.period
            is DashboardEvent.EntryDeleted -> handleDelete(event.entry)
            is DashboardEvent.UndoDelete -> handleUndoDelete()
            is DashboardEvent.SnackbarDismissed -> _pendingDeleteEntry.value = null
        }
    }

    private fun handleDelete(entry: BloodPressureEntry) {
        // Store for potential undo before issuing the DB delete.
        // If a previous entry was pending undo, it is silently committed (already deleted from DB).
        _pendingDeleteEntry.value = entry
        viewModelScope.launch {
            deleteMeasurementUseCase.execute(entry.id)
        }
    }

    private fun handleUndoDelete() {
        viewModelScope.launch {
            val entry = _pendingDeleteEntry.value ?: return@launch
            _pendingDeleteEntry.value = null
            // Re-insert with id = 0 so Room generates a fresh primary key,
            // avoiding a possible unique-constraint conflict.
            repository.insertEntry(entry.copy(id = 0))
        }
    }
}
