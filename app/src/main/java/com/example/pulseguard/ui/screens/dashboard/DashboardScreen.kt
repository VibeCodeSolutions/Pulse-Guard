// com.example.pulseguard.ui.screens.dashboard.DashboardScreen
package com.example.pulseguard.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pulseguard.R
import com.example.pulseguard.domain.model.BloodPressureCategory
import com.example.pulseguard.domain.model.DashboardAggregation
import com.example.pulseguard.domain.model.DashboardPeriod
import com.example.pulseguard.ui.components.BloodPressureCard
import com.example.pulseguard.ui.components.PressureChart
import com.example.pulseguard.ui.theme.PulseGuardTheme
import com.example.pulseguard.ui.theme.toColor
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * Dashboard screen showing aggregated blood pressure statistics, a trend chart,
 * and a list of recent measurements for the selected time period.
 *
 * All state comes from [DashboardViewModel] via a reactive [StateFlow]; no business
 * logic lives in this composable.
 *
 * Includes:
 * - FAB bounce animation via [MutableInteractionSource] + [animateFloatAsState].
 * - Staggered entrance animation on the [BloodPressureCard] list via
 *   [AnimatedVisibility] + [LaunchedEffect] delay, capped at 7 visible items
 *   (max 385 ms stagger) to keep the animation snappy regardless of list size.
 *
 * @param onAddEntry Lambda invoked when the user taps the FAB to add a new entry.
 * @param onExport   Lambda invoked when the user taps the TopAppBar export action.
 * @param viewModel  Koin-injected [DashboardViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddEntry: () -> Unit,
    onExport: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ── FAB bounce animation ───────────────────────────────────────────────
    val fabInteractionSource = remember { MutableInteractionSource() }
    val isFabPressed by fabInteractionSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "fab_scale",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = onExport) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.cd_export_action),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
                modifier = Modifier.scale(fabScale),
                interactionSource = fabInteractionSource,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.cd_add_entry),
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.onEvent(DashboardEvent.PeriodChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (uiState.isEmpty) {
                    item {
                        EmptyState(
                            onAddEntry = onAddEntry,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    uiState.aggregation?.let { aggregation ->
                        item {
                            SummaryCard(
                                aggregation = aggregation,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (uiState.chartData.size >= 2) {
                        item {
                            PressureChart(
                                chartData = uiState.chartData,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.dashboard_recent_entries),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // ── Staggered entrance animation ───────────────────────
                    itemsIndexed(
                        items = uiState.recentEntries,
                        key = { _, item -> "${item.id}_${item.timestamp}" },
                    ) { index, entry ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect("${entry.id}_${entry.timestamp}") {
                            // Cap stagger at 7 items (max 385 ms) so late list items
                            // don't wait forever when many entries are displayed.
                            delay(index.coerceAtMost(7) * 55L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(durationMillis = 250)) +
                                slideInVertically(
                                    animationSpec = tween(durationMillis = 250),
                                    initialOffsetY = { fullHeight -> fullHeight / 4 },
                                ),
                        ) {
                            BloodPressureCard(
                                entry = entry,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: DashboardPeriod,
    onPeriodSelected: (DashboardPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectorDescription = stringResource(R.string.cd_period_selector)
    val periods = DashboardPeriod.entries
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.semantics { contentDescription = selectorDescription },
    ) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
            ) {
                Text(text = stringResource(periodLabelResId(period)))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    aggregation: DashboardAggregation,
    modifier: Modifier = Modifier,
) {
    val categoryColor = remember(aggregation.category) { aggregation.category.toColor() }
    val categoryLabel = categoryLabel(aggregation.category)
    val badgeDescription = stringResource(R.string.cd_category_badge, categoryLabel)

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = aggregation.periodLabel,
                    style = MaterialTheme.typography.titleMedium,
                )
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryColor,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .semantics { contentDescription = badgeDescription },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatColumn(
                    label = stringResource(R.string.dashboard_summary_avg_systolic),
                    value = "%.0f".format(aggregation.avgSystolic),
                )
                StatColumn(
                    label = stringResource(R.string.dashboard_summary_avg_diastolic),
                    value = "%.0f".format(aggregation.avgDiastolic),
                )
                StatColumn(
                    label = stringResource(R.string.dashboard_summary_avg_pulse),
                    value = "%.0f".format(aggregation.avgPulse),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.dashboard_summary_entries, aggregation.entryCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmptyState(
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.height(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.dashboard_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.dashboard_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onAddEntry) {
            Text(stringResource(R.string.dashboard_empty_cta))
        }
    }
}

@Composable
private fun categoryLabel(category: BloodPressureCategory): String = when (category) {
    BloodPressureCategory.OPTIMAL -> stringResource(R.string.category_optimal)
    BloodPressureCategory.NORMAL -> stringResource(R.string.category_normal)
    BloodPressureCategory.HIGH_NORMAL -> stringResource(R.string.category_high_normal)
    BloodPressureCategory.HYPERTENSION_1 -> stringResource(R.string.category_hypertension_1)
    BloodPressureCategory.HYPERTENSION_2 -> stringResource(R.string.category_hypertension_2)
    BloodPressureCategory.HYPERTENSION_3 -> stringResource(R.string.category_hypertension_3)
}

private fun periodLabelResId(period: DashboardPeriod): Int = when (period) {
    DashboardPeriod.DAY -> R.string.dashboard_period_day
    DashboardPeriod.WEEK -> R.string.dashboard_period_week
    DashboardPeriod.MONTH -> R.string.dashboard_period_month
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    PulseGuardTheme {
        DashboardScreen(onAddEntry = {})
    }
}
