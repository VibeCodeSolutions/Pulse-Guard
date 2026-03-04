// com.example.pulseguard.ui.screens.dashboard.DashboardViewModel
package com.example.pulseguard.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Dashboard screen.
 *
 * Reacts to period selection via [MutableStateFlow] + [flatMapLatest]: switching
 * periods automatically cancels the previous database query and starts a new one.
 *
 * @param getDashboardDataUseCase Use case providing reactive dashboard data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(DashboardPeriod.WEEK)

    /**
     * Reactive UI state that updates whenever the selected period or the underlying
     * database data changes.
     */
    val uiState = _selectedPeriod
        .flatMapLatest { period ->
            getDashboardDataUseCase.observe(period)
                .map { data ->
                    DashboardUiState(
                        selectedPeriod = period,
                        aggregation = data.aggregation,
                        recentEntries = data.entries,
                        chartData = data.chartData,
                        isLoading = false,
                    )
                }
        }
        .stateIn(
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
            is DashboardEvent.EntryDeleted -> Unit // Phase 4: implement delete via repository
        }
    }
}
