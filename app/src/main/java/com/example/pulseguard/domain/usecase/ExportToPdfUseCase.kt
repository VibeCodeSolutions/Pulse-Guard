// com.example.pulseguard.domain.usecase.ExportToPdfUseCase
package com.example.pulseguard.domain.usecase

import android.net.Uri
import com.example.pulseguard.domain.repository.BloodPressureRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case that generates a PDF blood pressure report for a given date range
 * and returns a shareable [Uri].
 *
 * This use case is clean of Android framework dependencies (except [Uri], which
 * is used as a data holder) and delegates the actual generation to the repository
 * layer.
 *
 * All blocking work — [android.graphics.Canvas] drawing, [java.io.FileOutputStream]
 * writing, and Room's `.first()` collection — is pinned to [ioDispatcher] to keep
 * the main thread jank-free for large datasets. The dispatcher is injectable to
 * allow tests to substitute a [kotlinx.coroutines.test.TestCoroutineDispatcher]
 * without racing against real IO threads.
 *
 * @param repository   Source for generating the report.
 * @param ioDispatcher Dispatcher for blocking IO work; defaults to [Dispatchers.IO].
 */
class ExportToPdfUseCase(
    private val repository: BloodPressureRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * Generates the PDF on [ioDispatcher] and returns its URI.
     *
     * @param startTime Range start in Unix epoch millis (inclusive).
     * @param endTime   Range end in Unix epoch millis (inclusive).
     * @return [Result] wrapping the [Uri] on success.
     */
    suspend fun execute(startTime: Long, endTime: Long): Result<Uri> =
        withContext(ioDispatcher) {
            repository.generatePdfUri(startTime, endTime)
        }
}
