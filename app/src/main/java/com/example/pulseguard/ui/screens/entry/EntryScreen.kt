// com.example.pulseguard.ui.screens.entry.EntryScreen
package com.example.pulseguard.ui.screens.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pulseguard.R
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.ui.components.ArmSelector
import com.example.pulseguard.ui.components.MedicationToggle
import com.example.pulseguard.ui.components.NumericInputField
import com.example.pulseguard.ui.theme.PulseGuardTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Full-screen composable for entering a new blood pressure measurement.
 *
 * Follows strict Unidirectional Data Flow: the composable reads from
 * [EntryViewModel.uiState] and dispatches [EntryEvent]s. No business logic
 * lives here. The Number Pad stays visible throughout because every
 * [NumericInputField] uses [KeyboardType.Number].
 *
 * **Focus chain:** Systolisch → Diastolisch → Puls, enforced via
 * [FocusRequester] and [ImeAction.Next]. The Done action on Puls triggers save.
 *
 * **Save feedback:** On success, haptic feedback fires, a [SnackbarHost]
 * shows a confirmation, and [onNavigateBack] is called.
 *
 * @param onNavigateBack Lambda to pop this destination from the back stack.
 * @param viewModel      Koin-injected [EntryViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    onNavigateBack: () -> Unit,
    zoneId: ZoneId = ZoneId.systemDefault(),
    viewModel: EntryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    // ── FocusRequesters for the number-pad chain ──────────────────────────
    val systolicFocus = remember { FocusRequester() }
    val diastolicFocus = remember { FocusRequester() }
    val pulseFocus = remember { FocusRequester() }

    // ── Date / Time picker dialog state (local UI state only) ─────────────
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    // Bridges the date chosen in DatePickerDialog into the subsequent TimePickerDialog.
    var selectedDateMillis by remember(uiState.timestamp) { mutableStateOf(uiState.timestamp) }

    // ── Side effects ──────────────────────────────────────────────────────
    val saveSuccessMessage = stringResource(R.string.snackbar_save_success)

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            snackbarHostState.showSnackbar(
                message = saveSuccessMessage,
                duration = SnackbarDuration.Short,
            )
            onNavigateBack()
        }
    }

    // Claim focus on the first field immediately after composition
    LaunchedEffect(Unit) {
        systolicFocus.requestFocus()
    }

    // ── Date picker dialog ────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.timestamp,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: uiState.timestamp
                    showDatePicker = false
                    showTimePicker = true
                }) { Text(stringResource(R.string.dialog_next)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Time picker dialog ────────────────────────────────────────────────
    if (showTimePicker) {
        val initialTime = remember {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(uiState.timestamp), zoneId)
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.dialog_select_time)) },
            confirmButton = {
                TextButton(onClick = {
                    val combinedMillis = Instant.ofEpochMilli(selectedDateMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                        .atTime(timePickerState.hour, timePickerState.minute)
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()
                    viewModel.onEvent(EntryEvent.TimestampChanged(combinedMillis))
                    showTimePicker = false
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    // ── Screen ────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.entry_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Timestamp (tappable)
            TimestampRow(
                timestamp = uiState.timestamp,
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                zoneId = zoneId,
            )

            // 2a. Systolisch
            NumericInputField(
                value = uiState.systolic,
                onValueChange = { viewModel.onEvent(EntryEvent.SystolicChanged(it)) },
                label = stringResource(R.string.label_systolic),
                modifier = Modifier.fillMaxWidth(),
                errorMessage = uiState.visibleErrors[EntryUiState.FIELD_SYSTOLIC]
                    ?.let { stringResource(it) },
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { diastolicFocus.requestFocus() },
                ),
                focusRequester = systolicFocus,
                onAutoAdvance = { diastolicFocus.requestFocus() },
            )

            // 2b. Diastolisch
            NumericInputField(
                value = uiState.diastolic,
                onValueChange = { viewModel.onEvent(EntryEvent.DiastolicChanged(it)) },
                label = stringResource(R.string.label_diastolic),
                modifier = Modifier.fillMaxWidth(),
                errorMessage = uiState.visibleErrors[EntryUiState.FIELD_DIASTOLIC]
                    ?.let { stringResource(it) },
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { pulseFocus.requestFocus() },
                ),
                focusRequester = diastolicFocus,
                onAutoAdvance = { pulseFocus.requestFocus() },
            )

            // 2c. Puls – Done triggers save
            NumericInputField(
                value = uiState.pulse,
                onValueChange = { viewModel.onEvent(EntryEvent.PulseChanged(it)) },
                label = stringResource(R.string.label_pulse),
                modifier = Modifier.fillMaxWidth(),
                errorMessage = uiState.visibleErrors[EntryUiState.FIELD_PULSE]
                    ?.let { stringResource(it) },
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.onEvent(EntryEvent.SaveClicked) },
                ),
                focusRequester = pulseFocus,
            )

            HorizontalDivider()

            // 3. Arm selector
            Text(
                text = stringResource(R.string.label_measurement_arm),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ArmSelector(
                selectedArm = uiState.measurementArm,
                onArmSelected = { viewModel.onEvent(EntryEvent.ArmChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            // 4. Medication toggle
            MedicationToggle(
                checked = uiState.medicationTaken,
                onCheckedChange = { viewModel.onEvent(EntryEvent.MedicationToggled) },
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            // 5. Save button
            val saveButtonDescription = stringResource(R.string.cd_save_button)
            Button(
                onClick = { viewModel.onEvent(EntryEvent.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = saveButtonDescription },
                enabled = uiState.isSaveEnabled,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.button_save))
                }
            }
        }
    }
}

/**
 * Clickable card showing the formatted measurement timestamp.
 *
 * Tapping the card opens the date/time picker flow.
 *
 * @param zoneId Time zone used for formatting [timestamp]. Matches the zone passed to [EntryScreen].
 */
@Composable
private fun TimestampRow(
    timestamp: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zoneId: ZoneId = ZoneId.systemDefault(),
) {
    val formatted = remember(timestamp, zoneId) {
        val localDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            zoneId,
        )
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .format(localDateTime)
    }

    val label = stringResource(R.string.label_measurement_time)
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = "$label: $formatted"
            role = Role.Button
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_measurement_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Icon(
                    imageVector = Icons.Filled.EditCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EntryScreenPreview() {
    PulseGuardTheme {
        EntryScreen(onNavigateBack = {})
    }
}
