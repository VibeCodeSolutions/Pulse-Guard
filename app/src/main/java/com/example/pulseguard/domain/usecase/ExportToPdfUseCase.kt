// com.example.pulseguard.domain.usecase.ExportToPdfUseCase
package com.example.pulseguard.domain.usecase

import android.net.Uri
import com.example.pulseguard.domain.repository.BloodPressureRepository

/**
 * Use case that generates a PDF blood pressure report for a given date range
 * and returns a shareable [Uri].
 *
 * This use case is now clean of Android framework dependencies (except Uri, 
 * which is used as a data holder) and delegates the actual generation to 
 * the repository layer.
 *
 * @param repository Source for generating the report.
 */
class ExportToPdfUseCase(
    private val repository: BloodPressureRepository,
) {

    /**
     * Generates the PDF and returns its URI.
     *
     * @param startTime Range start in Unix epoch millis (inclusive).
     * @param endTime   Range end in Unix epoch millis (inclusive).
     * @return [Result] wrapping the [Uri] on success.
     */
    suspend fun execute(startTime: Long, endTime: Long): Result<Uri> {
        return repository.generatePdfUri(startTime, endTime)
    }
}
