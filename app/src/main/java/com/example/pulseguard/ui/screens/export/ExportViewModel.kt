// com.example.pulseguard.ui.screens.export.ExportViewModel
package com.example.pulseguard.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseguard.domain.repository.BloodPressureRepository
import com.example.pulseguard.domain.usecase.ExportToPdfUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the PDF export screen.
 *
 * Receives [ExportEvent]s from the UI and produces an immutable [ExportUiState]
 * via a [StateFlow]. PDF generation is delegated to [ExportToPdfUseCase] and
 * runs on the [viewModelScope] coroutine scope.
 *
 * @param exportToPdfUseCase Use case responsible for PDF file generation.
 * @param repository         Data source used to count entries for the preview label.
 */
class ExportViewModel(
    private val exportToPdfUseCase: ExportToPdfUseCase,
    private val repository: BloodPressureRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())

    /** Observable UI state. Collect with [androidx.lifecycle.compose.collectAsStateWithLifecycle]. */
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    /**
     * Handles a UI event dispatched from [ExportScreen].
     *
     * @param event The event to process.
     */
    fun onEvent(event: ExportEvent) {
        when (event) {
            is ExportEvent.DateRangeSelected -> handleDateRangeSelected(event.startTime, event.endTime)
            is ExportEvent.GenerateClicked -> generatePdf()
            is ExportEvent.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun handleDateRangeSelected(startTime: Long, endTime: Long) {
        _uiState.update {
            it.copy(
                dateRangeStart = startTime,
                dateRangeEnd = endTime,
                generatedPdfUri = null,
            )
        }
        viewModelScope.launch {
            val count = repository.getEntriesForDateRange(startTime, endTime).first().size
            _uiState.update { it.copy(previewEntryCount = count) }
        }
    }

    private fun generatePdf() {
        val start = _uiState.value.dateRangeStart ?: return
        val end = _uiState.value.dateRangeEnd ?: return

        _uiState.update { it.copy(isGenerating = true, generatedPdfUri = null) }

        viewModelScope.launch {
            exportToPdfUseCase.execute(start, end)
                .onSuccess { uri ->
                    _uiState.update { it.copy(isGenerating = false, generatedPdfUri = uri) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = throwable.localizedMessage
                                ?: "Fehler bei der PDF-Generierung",
                        )
                    }
                }
        }
    }
}
