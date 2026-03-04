// com.example.pulseguard.ui.screens.export.ExportUiState
package com.example.pulseguard.ui.screens.export

import android.net.Uri

/**
 * Immutable state for the PDF export screen.
 *
 * @property dateRangeStart  Selected start of the export range in epoch millis, or `null` if not yet set.
 * @property dateRangeEnd    Selected end of the export range in epoch millis, or `null` if not yet set.
 * @property previewEntryCount Number of entries in the currently selected date range.
 * @property isGenerating    `true` while the PDF is being generated asynchronously.
 * @property generatedPdfUri [FileProvider] URI of the successfully generated PDF, or `null`.
 * @property errorMessage    Non-null when an error occurred during PDF generation; `null` otherwise.
 */
data class ExportUiState(
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val previewEntryCount: Int = 0,
    val isGenerating: Boolean = false,
    val generatedPdfUri: Uri? = null,
    val errorMessage: String? = null,
) {
    /** `true` when both start and end dates are selected. */
    val isDateRangeSelected: Boolean get() = dateRangeStart != null && dateRangeEnd != null

    /** `true` when generation can be triggered. */
    val canGenerate: Boolean get() = isDateRangeSelected && !isGenerating && previewEntryCount > 0

    /** `true` when the share button should be enabled. */
    val canShare: Boolean get() = generatedPdfUri != null && !isGenerating
}
