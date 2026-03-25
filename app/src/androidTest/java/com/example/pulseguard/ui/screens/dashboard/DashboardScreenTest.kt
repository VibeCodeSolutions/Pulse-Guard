// com.example.pulseguard.ui.screens.dashboard.DashboardScreenTest
package com.example.pulseguard.ui.screens.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pulseguard.data.local.entity.AggregatedValues
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.data.local.entity.MinMaxValues
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import com.example.pulseguard.domain.usecase.DeleteMeasurementUseCase
import kotlinx.coroutines.flow.map
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

/**
 * UI tests for [DashboardScreen].
 *
 * Verifies that period-selector chips are rendered and tappable, and that
 * the empty-state is shown when the repository is empty. A fake repository
 * is provided via Koin so tests run entirely in-process.
 */
@RunWith(AndroidJUnit4::class)
class DashboardScreenTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: DashboardFakeRepository

    @Before
    fun setUp() {
        fakeRepository = DashboardFakeRepository()
        startKoin {
            modules(
                module {
                    single<com.example.pulseguard.domain.repository.BloodPressureRepository> { fakeRepository }
                    factory { GetDashboardDataUseCase(get()) }
                    factory { DeleteMeasurementUseCase(get()) }
                    viewModel { DashboardViewModel(get(), get(), get()) }
                },
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // ── Period selector ───────────────────────────────────────────────────

    @Test
    fun dashboardScreen_periodSelectorDisplayed_todayChipIsVisible() {
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.onNodeWithText("Heute", substring = true).assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_periodSelectorDisplayed_weekChipIsVisible() {
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.onNodeWithText("7", substring = true).assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_periodSelectorDisplayed_monthChipIsVisible() {
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.onNodeWithText("30", substring = true).assertIsDisplayed()
    }

    // ── Period chip interaction ───────────────────────────────────────────

    @Test
    fun dashboardScreen_tapTodayChip_doesNotCrash() {
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.onNodeWithText("Heute", substring = true).performClick()
        composeTestRule.waitForIdle()
        // No crash = pass; period UI updates are covered in DashboardViewModelTest
    }

    @Test
    fun dashboardScreen_tapMonthChip_doesNotCrash() {
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.onNodeWithText("30", substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    // ── Empty state ───────────────────────────────────────────────────────

    @Test
    fun dashboardScreen_emptyRepository_emptyStateIsDisplayed() {
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.waitForIdle()
        // The empty state text is shown when there are no measurements
        composeTestRule.onNodeWithText("Keine Messungen", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    // ── FAB ───────────────────────────────────────────────────────────────

    @Test
    fun dashboardScreen_fabIsDisplayed() {
        var addEntryClicked = false
        composeTestRule.setContent {
            DashboardScreen(
                onAddEntry = { addEntryClicked = true },
                onExport = {},
            )
        }
        // The FAB uses contentDescription "Neue Messung hinzufügen"
        composeTestRule.onNodeWithText("Neue", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    // ── Entry display ─────────────────────────────────────────────────────

    @Test
    fun dashboardScreen_withOneEntry_entryCardIsDisplayed() {
        fakeRepository.seedEntries(
            listOf(
                BloodPressureEntry(
                    systolic = 125,
                    diastolic = 82,
                    pulse = 70,
                    measurementArm = MeasurementArm.LEFT,
                    medicationTaken = false,
                    timestamp = System.currentTimeMillis(),
                ),
            ),
        )
        composeTestRule.setContent {
            DashboardScreen(onAddEntry = {}, onExport = {})
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("125", substring = true).assertIsDisplayed()
    }
}

/**
 * In-process fake repository for DashboardScreen tests.
 */
private class DashboardFakeRepository :
    com.example.pulseguard.domain.repository.BloodPressureRepository {

    private val _entries =
        kotlinx.coroutines.flow.MutableStateFlow<List<BloodPressureEntry>>(emptyList())

    fun seedEntries(entries: List<BloodPressureEntry>) {
        _entries.value = entries
    }

    override fun getAllEntries() = _entries

    override fun getEntriesForDateRange(startTime: Long, endTime: Long) =
        _entries.map { list -> list.filter { it.timestamp in startTime..endTime } }

    override fun getAverageForDateRange(startTime: Long, endTime: Long) =
        _entries.map { list ->
            val filtered = list.filter { it.timestamp in startTime..endTime }
            if (filtered.isEmpty()) null
            else AggregatedValues(
                avgSystolic = filtered.map { it.systolic }.average().toFloat(),
                avgDiastolic = filtered.map { it.diastolic }.average().toFloat(),
                avgPulse = filtered.map { it.pulse }.average().toFloat(),
            )
        }

    override fun getMinMaxForDateRange(startTime: Long, endTime: Long) =
        _entries.map { list ->
            val filtered = list.filter { it.timestamp in startTime..endTime }
            if (filtered.isEmpty()) null
            else MinMaxValues(
                minSystolic = filtered.minOf { it.systolic },
                maxSystolic = filtered.maxOf { it.systolic },
                minDiastolic = filtered.minOf { it.diastolic },
                maxDiastolic = filtered.maxOf { it.diastolic },
            )
        }

    override fun getEntryCount() = _entries.map { it.size }

    override suspend fun insertEntry(entry: BloodPressureEntry): Long = 0L

    override suspend fun deleteEntry(id: Long) {}

    override suspend fun generatePdfUri(startTime: Long, endTime: Long): Result<android.net.Uri> =
        Result.failure(UnsupportedOperationException())
}
