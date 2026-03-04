// com.example.pulseguard.domain.usecase.DeleteMeasurementUseCase
package com.example.pulseguard.domain.usecase

import com.example.pulseguard.domain.repository.BloodPressureRepository

/**
 * Use case that permanently removes a single blood pressure measurement.
 *
 * Delegating to this use case keeps the ViewModel free of repository references
 * for write operations while leaving the delete path open to future business-rule
 * extensions (e.g. audit logging, cloud sync).
 *
 * @param repository Data source used to perform the deletion.
 */
class DeleteMeasurementUseCase(private val repository: BloodPressureRepository) {

    /**
     * Deletes the entry with the given [id] from the repository.
     *
     * The operation is idempotent: deleting a non-existent id is a no-op.
     *
     * @param id Primary key of the entry to remove.
     */
    suspend fun execute(id: Long) {
        repository.deleteEntry(id)
    }
}
