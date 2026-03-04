// com.example.pulseguard.ui.components.BloodPressureCard
package com.example.pulseguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pulseguard.R
import com.example.pulseguard.data.local.entity.BloodPressureEntry
import com.example.pulseguard.domain.model.BloodPressureCategory
import com.example.pulseguard.domain.model.MeasurementArm
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Card displaying a single blood pressure measurement summary.
 *
 * A 4 dp colored leading strip indicates the [BloodPressureCategory] at a glance.
 * The category name is always rendered as text alongside the color for accessibility.
 * The full measurement detail is exposed via [Modifier.semantics] for screen readers.
 *
 * @param entry    The blood pressure measurement to display.
 * @param modifier Optional [Modifier].
 */
@Composable
fun BloodPressureCard(
    entry: BloodPressureEntry,
    modifier: Modifier = Modifier,
) {
    val category = remember(entry.systolic, entry.diastolic) {
        BloodPressureCategory.fromValues(entry.systolic, entry.diastolic)
    }
    val categoryColor = remember(category) {
        Color(android.graphics.Color.parseColor(category.colorHex))
    }
    val categoryLabel = categoryLabel(category)

    val formattedDateTime = remember(entry.timestamp) {
        val ldt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entry.timestamp),
            ZoneId.systemDefault(),
        )
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ldt)
    }

    val armLabel = if (entry.measurementArm == MeasurementArm.LEFT) {
        stringResource(R.string.label_arm_left)
    } else {
        stringResource(R.string.label_arm_right)
    }

    val contentDesc = "$formattedDateTime – ${entry.systolic}/${entry.diastolic} mmHg, " +
        "Puls ${entry.pulse}, $armLabel, $categoryLabel"

    Card(
        modifier = modifier
            .heightIn(min = 72.dp)
            .semantics { contentDescription = contentDesc },
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(categoryColor),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedDateTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${entry.systolic}/${entry.diastolic} mmHg · ${entry.pulse} bpm",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
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
