// com.example.pulseguard.ui.screens.reminder.ReminderScreen
package com.example.pulseguard.ui.screens.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pulseguard.R
import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.data.local.entity.daysSet
import com.example.pulseguard.domain.model.ReminderType
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Screen displaying configured reminders with an add/edit dialog.
 *
 * Requests `POST_NOTIFICATIONS` permission on first interaction when needed
 * (Android 13+).
 *
 * @param onNavigateBack Lambda invoked when the user taps the back button.
 * @param viewModel      Koin-injected [ReminderViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Check initial permission state.
    LaunchedEffect(Unit) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed below API 33
        }
        viewModel.onEvent(ReminderEvent.PermissionResult(granted))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onEvent(ReminderEvent.PermissionResult(granted))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminder_screen_title)) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!uiState.hasNotificationPermission &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
                        permissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    }
                    viewModel.onEvent(ReminderEvent.AddClicked)
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.reminder_add),
                )
            }
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.reminders.isEmpty()) {
            ReminderEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(
                    items = uiState.reminders,
                    key = { it.id },
                ) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { enabled ->
                            viewModel.onEvent(
                                ReminderEvent.ToggleClicked(reminder.id, enabled),
                            )
                        },
                        onEdit = {
                            viewModel.onEvent(ReminderEvent.EditClicked(reminder))
                        },
                        onDelete = {
                            viewModel.onEvent(ReminderEvent.DeleteClicked(reminder.id))
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Add/Edit dialog
    if (uiState.showDialog) {
        ReminderDialog(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderCard(
    reminder: Reminder,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeText = "%02d:%02d".format(reminder.hour, reminder.minute)
    val daysText = reminder.daysSet
        .sortedBy { it.value }
        .joinToString(", ") {
            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    val typeIcon = when (reminder.type) {
        ReminderType.MEDICATION -> Icons.Filled.Medication
        ReminderType.MEASUREMENT -> Icons.Filled.MonitorHeart
    }

    Card(onClick = onEdit, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = typeIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.label,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "$timeText · $daysText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.reminder_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReminderDialog(
    uiState: ReminderUiState,
    onEvent: (ReminderEvent) -> Unit,
) {
    val title = if (uiState.editingReminderId != null) {
        stringResource(R.string.reminder_dialog_edit_title)
    } else {
        stringResource(R.string.reminder_dialog_add_title)
    }

    val timePickerState = rememberTimePickerState(
        initialHour = uiState.dialogHour,
        initialMinute = uiState.dialogMinute,
        is24Hour = true,
    )

    // Sync time picker state back to ViewModel when it changes.
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onEvent(ReminderEvent.DialogTimeChanged(timePickerState.hour, timePickerState.minute))
    }

    AlertDialog(
        onDismissRequest = { onEvent(ReminderEvent.DialogDismiss) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.dialogLabel,
                    onValueChange = { onEvent(ReminderEvent.DialogLabelChanged(it)) },
                    label = { Text(stringResource(R.string.reminder_label_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Type selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ReminderType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = uiState.dialogType == type,
                            onClick = { onEvent(ReminderEvent.DialogTypeChanged(type)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ReminderType.entries.size,
                            ),
                        ) {
                            Text(
                                text = when (type) {
                                    ReminderType.MEDICATION ->
                                        stringResource(R.string.reminder_type_medication)
                                    ReminderType.MEASUREMENT ->
                                        stringResource(R.string.reminder_type_measurement)
                                },
                            )
                        }
                    }
                }

                // Time picker
                TimePicker(state = timePickerState)

                // Day-of-week chips
                Text(
                    text = stringResource(R.string.reminder_days_label),
                    style = MaterialTheme.typography.labelMedium,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DayOfWeek.entries.forEach { day ->
                        FilterChip(
                            selected = day in uiState.dialogDays,
                            onClick = { onEvent(ReminderEvent.DialogDayToggled(day)) },
                            label = {
                                Text(
                                    day.getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.getDefault(),
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(ReminderEvent.DialogSave) },
                enabled = uiState.isDialogSaveEnabled,
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(ReminderEvent.DialogDismiss) }) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Composable
private fun ReminderEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MonitorHeart,
            contentDescription = null,
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.reminder_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.reminder_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
