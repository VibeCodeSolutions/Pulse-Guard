// com.example.pulseguard.domain.usecase.ExportToPdfUseCaseTest
package com.example.pulseguard.domain.usecase

import android.net.Uri
import com.example.pulseguard.fake.FakeBloodPressureRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ExportToPdfUseCase].
 *
 * The use case is a thin delegation layer; tests verify that it faithfully
 * forwards the repository result to the caller in both success and failure paths.
 *
 * Note: [android.net.Uri] is an Android framework type. With
 * `testOptions { unitTests { isReturnDefaultValues = true } }` in build.gradle.kts,
 * Android stubs return `null` for object-returning methods, so [Uri.parse] returns
 * null. The test validates [Result.isSuccess] / [Result.isFailure] only — it never
 * dereferences the [Uri] value.
 */
class ExportToPdfUseCaseTest {

    private lateinit var fakeRepo: FakeBloodPressureRepository
    private lateinit var useCase: ExportToPdfUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeBloodPressureRepository()
        useCase = ExportToPdfUseCase(fakeRepo)
    }

    @Test
    fun execute_repositoryReturnsSuccess_resultIsSuccess() = runTest {
        // Uri is an Android framework type. With isReturnDefaultValues = true, its factory
        // methods return null. We use an unchecked cast to produce a non-null Result<Uri>
        // without calling any Uri methods — only result.isSuccess is asserted below.
        @Suppress("UNCHECKED_CAST")
        fakeRepo.generatePdfResult = Result.success(Unit) as Result<Uri>

        val result = useCase.execute(startTime = 0L, endTime = Long.MAX_VALUE)

        assertTrue(result.isSuccess)
    }

    @Test
    fun execute_repositoryReturnsFailure_resultIsFailure() = runTest {
        fakeRepo.generatePdfResult = Result.failure(RuntimeException("Disk full"))

        val result = useCase.execute(startTime = 0L, endTime = Long.MAX_VALUE)

        assertTrue(result.isFailure)
    }

    @Test
    fun execute_repositoryReturnsFailure_exceptionIsPreserved() = runTest {
        val cause = RuntimeException("Disk full")
        fakeRepo.generatePdfResult = Result.failure(cause)

        val result = useCase.execute(startTime = 0L, endTime = Long.MAX_VALUE)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
}
