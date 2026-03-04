// com.example.pulseguard.ui.components.PressureChart
package com.example.pulseguard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pulseguard.R
import com.example.pulseguard.domain.model.ChartDataPoint
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

/**
 * Displays a dual-line trend chart for systolic and diastolic blood pressure values.
 *
 * Renders nothing when fewer than 2 data points are provided — a line chart
 * requires at least two points to draw. The chart updates reactively whenever
 * [chartData] changes.
 *
 * @param chartData List of [ChartDataPoint] sorted oldest-first.
 * @param modifier  Optional [Modifier].
 */
@Composable
fun PressureChart(
    chartData: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
) {
    if (chartData.size < 2) return

    val systolicColor = MaterialTheme.colorScheme.primary
    val diastolicColor = MaterialTheme.colorScheme.tertiary
    val chartContentDescription = stringResource(R.string.cd_pressure_chart)

    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(chartData) {
        val systolicEntries = chartData.map { entryOf(it.x, it.systolic) }
        val diastolicEntries = chartData.map { entryOf(it.x, it.diastolic) }
        modelProducer.setEntriesSuspending(systolicEntries, diastolicEntries)
    }

    // Remembered so that lineSpec objects are not re-allocated on every recomposition;
    // keys are the colors, which only change on theme switch.
    val systolicSpec = remember(systolicColor) { lineSpec(lineColor = systolicColor) }
    val diastolicSpec = remember(diastolicColor) { lineSpec(lineColor = diastolicColor) }

    Column(modifier = modifier) {
        Chart(
            chart = lineChart(lines = listOf(systolicSpec, diastolicSpec)),
            chartModelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = chartContentDescription },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Surface(
                color = systolicColor,
                shape = CircleShape,
                modifier = Modifier.size(8.dp),
            ) {}
            Text(
                text = stringResource(R.string.dashboard_legend_systolic),
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = diastolicColor,
                shape = CircleShape,
                modifier = Modifier.size(8.dp),
            ) {}
            Text(
                text = stringResource(R.string.dashboard_legend_diastolic),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
