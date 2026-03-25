package com.example.pulseguard.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pulseguard.domain.repository.BloodPressureRepository
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import com.example.pulseguard.domain.usecase.DeleteMeasurementUseCase
import com.example.pulseguard.domain.usecase.ExportToPdfUseCase
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import com.example.pulseguard.ui.screens.dashboard.DashboardViewModel
import com.example.pulseguard.ui.screens.entry.EntryViewModel
import com.example.pulseguard.ui.screens.export.ExportViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class PulseGuardNavGraphTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeRepository = object : BloodPressureRepository {
        override fun getAllEntries() = MutableStateFlow(emptyList<com.example.pulseguard.data.local.entity.BloodPressureEntry>())
        override fun getEntriesForDateRange(start: Long, end: Long) = MutableStateFlow(emptyList<com.example.pulseguard.data.local.entity.BloodPressureEntry>())
        override fun getAverageForDateRange(start: Long, end: Long) = MutableStateFlow(null)
        override fun getMinMaxForDateRange(start: Long, end: Long) = MutableStateFlow(null)
        override fun getEntryCount() = MutableStateFlow(0)
        override suspend fun insertEntry(entry: com.example.pulseguard.data.local.entity.BloodPressureEntry) = 0L
        override suspend fun deleteEntry(id: Long) {}
        override suspend fun generatePdfUri(startTime: Long, endTime: Long): Result<android.net.Uri> =
            Result.failure(UnsupportedOperationException())
    }

    @Before
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<BloodPressureRepository> { fakeRepository }
                    factory { AddMeasurementUseCase(get()) }
                    factory { DeleteMeasurementUseCase(get()) }
                    factory { GetDashboardDataUseCase(get()) }
                    factory { ExportToPdfUseCase(get()) }
                    viewModel { DashboardViewModel(get(), get(), get()) }
                    viewModel { EntryViewModel(get()) }
                    viewModel { ExportViewModel(get(), get()) }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun globalFooter_isVisibleOnDashboard() {
        composeTestRule.setContent {
            PulseGuardNavGraph()
        }
        composeTestRule.onNodeWithText("VibeCode Solutions", substring = true).assertIsDisplayed()
    }
}
