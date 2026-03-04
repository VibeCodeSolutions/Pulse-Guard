// com.example.pulseguard.ui.screens.export.ExportScreen
package com.example.pulseguard.ui.screens.export

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pulseguard.R
import com.example.pulseguard.ui.theme.PulseGuardTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Export screen that allows users to choose a date range, generate a PDF report,
 * and share it via the Android share sheet.
 *
 * All state is owned by [ExportViewModel]; no business logic runs in this composable.
 *
 * @param onNavigateBack Lambda invoked when the user taps the back arrow.
 * @param viewModel      Koin-injected [ExportViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Show error in snackbar
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ExportEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_screen_title)) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            DateRangeSection(
                startTime = uiState.dateRangeStart,
                endTime = uiState.dateRangeEnd,
                onRangeSelected = { start, end ->
                    viewModel.onEvent(ExportEvent.DateRangeSelected(start, end))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            PreviewCountCard(
                isDateRangeSelected = uiState.isDateRangeSelected,
                entryCount = uiState.previewEntryCount,
                modifier = Modifier.fillMaxWidth(),
            )

            GenerateButton(
                canGenerate = uiState.canGenerate,
                isGenerating = uiState.isGenerating,
                onGenerateClicked = { viewModel.onEvent(ExportEvent.GenerateClicked) },
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.canShare) {
                val shareDescription = stringResource(R.string.cd_share_pdf)
                val chooserTitle = stringResource(R.string.export_chooser_title)
                val pdfUri = uiState.generatedPdfUri
                if (pdfUri != null) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, pdfUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = shareDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.export_button_share),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    Text(
                        text = stringResource(R.string.export_pdf_ready),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSection(
    startTime: Long?,
    endTime: Long?,
    onRangeSelected: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val zone = remember { ZoneId.systemDefault() }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    fun Long?.toDisplayDate(): String = this
        ?.let { Instant.ofEpochMilli(it).atZone(zone).format(formatter) }
        ?: ""

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.export_section_date_range),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val startCd = stringResource(R.string.cd_start_date_picker)
            OutlinedButton(
                onClick = { showStartPicker = true },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = startCd },
            ) {
                Text(
                    text = if (startTime != null) {
                        startTime.toDisplayDate()
                    } else {
                        stringResource(R.string.export_start_date_label)
                    },
                )
            }

            val endCd = stringResource(R.string.cd_end_date_picker)
            OutlinedButton(
                onClick = { showEndPicker = true },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = endCd },
            ) {
                Text(
                    text = if (endTime != null) {
                        endTime.toDisplayDate()
                    } else {
                        stringResource(R.string.export_end_date_label)
                    },
                )
            }
        }
    }

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = startTime,
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selectedStart ->
                            val end = endTime ?: selectedStart
                            val effectiveEnd = if (end < selectedStart) selectedStart else end
                            onRangeSelected(selectedStart, effectiveEnd)
                        }
                        showStartPicker = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = endTime,
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selectedEnd ->
                            val start = startTime ?: selectedEnd
                            val effectiveStart = if (start > selectedEnd) selectedEnd else start
                            onRangeSelected(effectiveStart, selectedEnd)
                        }
                        showEndPicker = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun PreviewCountCard(
    isDateRangeSelected: Boolean,
    entryCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        val message = when {
            !isDateRangeSelected -> stringResource(R.string.export_select_dates)
            entryCount == 0 -> stringResource(R.string.export_no_entries)
            else -> stringResource(R.string.export_preview_count, entryCount)
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun GenerateButton(
    canGenerate: Boolean,
    isGenerating: Boolean,
    onGenerateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val generateCd = stringResource(R.string.cd_generate_pdf)
    Button(
        onClick = onGenerateClicked,
        enabled = canGenerate,
        modifier = modifier.semantics { contentDescription = generateCd },
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = stringResource(R.string.export_generating),
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            Text(text = stringResource(R.string.export_button_generate))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExportScreenPreview() {
    PulseGuardTheme {
        ExportScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ExportScreenWithSelectionPreview() {
    PulseGuardTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewCountCard(isDateRangeSelected = true, entryCount = 42)
            GenerateButton(canGenerate = true, isGenerating = false, onGenerateClicked = {})
        }
    }
}

/** Extension for display formatting; scoped to this file only. */
private fun Long.toDisplayDate(zone: ZoneId, formatter: DateTimeFormatter): String =
    Instant.ofEpochMilli(this).atZone(zone).format(formatter)
