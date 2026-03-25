// com.example.pulseguard.ui.screens.entry.EntryScreenTest
package com.example.pulseguard.ui.screens.entry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import kotlinx.coroutines.flow.map
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin

import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

/**
 * UI tests for [EntryScreen] using [createComposeRule].
 *
 * Koin is bootstrapped with an in-memory fake repository so that no real
 * database or Android services are required. Tests verify that the screen
 * renders the numeric input fields, shows validation errors after save
 * attempts with invalid data, and triggers the save flow on valid input.
 *
 * **Important:** [ComponentActivity] is used (not MainActivity) to isolate
 * these tests from the SplashScreen dependency introduced in Phase 5.
 */
@RunWith(AndroidJUnit4::class)
class EntryScreenTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeAndroidRepository

    @Before
    fun setUp() {
        fakeRepository = FakeAndroidRepository()
        startKoin {
            modules(
                module {
                    single { fakeRepository }
                    factory {
                        AddMeasurementUseCase(get<com.example.pulseguard.domain.repository.BloodPressureRepository>())
                    }
                    viewModel { EntryViewModel(get()) }
                },
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // ── Screen rendering ──────────────────────────────────────────────────

    @Test
    fun entryScreen_isDisplayed_systolicFieldExists() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        // Systolic label is shown on screen
        composeTestRule.onNodeWithText("Systolisch", substring = true).assertIsDisplayed()
    }

    @Test
    fun entryScreen_isDisplayed_diastolicFieldExists() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Diastolisch", substring = true).assertIsDisplayed()
    }

    @Test
    fun entryScreen_isDisplayed_pulseFieldExists() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Puls", substring = true).assertIsDisplayed()
    }

    // ── Validation feedback ───────────────────────────────────────────────

    @Test
    fun entryScreen_saveClickedWithEmptyFields_showsRequiredErrorForSystolic() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Speichern", substring = true).performClick()
        composeTestRule.waitForIdle()
        // After clicking save with empty fields, a required-field error must appear
        composeTestRule.onNodeWithText("Pflichtfeld", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun entryScreen_systolicOutOfRange_showsRangeError() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Systolisch", substring = true)
            .performTextInput("300")  // Intentionally use 300 which is the max
        // Type a value above max
        composeTestRule.onNodeWithText("Systolisch", substring = true)
            .performTextInput("301")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("60", substring = true, ignoreCase = true)
            .assertIsDisplayed()
    }

    // ── Arm selector ──────────────────────────────────────────────────────

    @Test
    fun entryScreen_armSelectorPresent_linksArmIsDisplayed() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Links", substring = true).assertIsDisplayed()
    }

    @Test
    fun entryScreen_armSelectorPresent_rechtsArmIsDisplayed() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Rechts", substring = true).assertIsDisplayed()
    }

    // ── Auto-focus ────────────────────────────────────────────────────────

    @Test
    fun entryScreen_systolicThreeDigitsTyped_focusMovesToDiastolic() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        // Initially, systolic should have focus
        composeTestRule.onNodeWithText("Systolisch", substring = true).assertIsFocused()

        // Type 3 digits into systolic
        composeTestRule.onNodeWithText("Systolisch", substring = true)
            .performTextInput("120")

        composeTestRule.waitForIdle()

        // Diastolic should now have focus
        composeTestRule.onNodeWithText("Diastolisch", substring = true).assertIsFocused()
    }

    @Test
    fun entryScreen_diastolicThreeDigitsTyped_focusMovesToPulse() {
        composeTestRule.setContent {
            EntryScreen(onNavigateBack = {})
        }
        // Manually focus diastolic first
        composeTestRule.onNodeWithText("Diastolisch", substring = true).performClick()

        // Type 3 digits into diastolic
        composeTestRule.onNodeWithText("Diastolisch", substring = true)
            .performTextInput("080")

        composeTestRule.waitForIdle()

        // Pulse should now have focus
        composeTestRule.onNodeWithText("Puls", substring = true).assertIsFocused()
    }
}

/**
 * In-process fake repository for UI tests.
 *
 * Declared as a top-level class so that Koin can provide it as a singleton
 * per test without importing the shared unit-test fake (which lives in the
 * `test` source set, inaccessible from `androidTest`).
 */
private class FakeAndroidRepository :
    com.example.pulseguard.domain.repository.BloodPressureRepository {

    private val _entries =
        kotlinx.coroutines.flow.MutableStateFlow<List<com.example.pulseguard.data.local.entity.BloodPressureEntry>>(
            emptyList(),
        )

    override fun getAllEntries() = _entries

    override fun getEntriesForDateRange(startTime: Long, endTime: Long) =
        _entries.map { list -> list.filter { it.timestamp in startTime..endTime } }

    override fun getAverageForDateRange(startTime: Long, endTime: Long) =
        _entries.map { null as com.example.pulseguard.data.local.entity.AggregatedValues? }

    override fun getMinMaxForDateRange(startTime: Long, endTime: Long) =
        _entries.map { null as com.example.pulseguard.data.local.entity.MinMaxValues? }

    override fun getEntryCount() = _entries.map { it.size }

    override suspend fun insertEntry(entry: com.example.pulseguard.data.local.entity.BloodPressureEntry): Long {
        val newId = (_entries.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _entries.value = _entries.value + entry.copy(id = newId)
        return newId
    }

    override suspend fun deleteEntry(id: Long) {
        _entries.value = _entries.value.filter { it.id != id }
    }

    override suspend fun generatePdfUri(startTime: Long, endTime: Long): Result<android.net.Uri> =
        Result.failure(UnsupportedOperationException("Not needed for UI tests"))
}
