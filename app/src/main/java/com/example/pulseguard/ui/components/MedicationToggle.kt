// com.example.pulseguard.ui.components.MedicationToggle
package com.example.pulseguard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pulseguard.R
import com.example.pulseguard.ui.theme.PulseGuardTheme

/**
 * Row composable combining a medication icon, a descriptive label, and a
 * [Switch] for toggling whether medication was taken before the measurement.
 *
 * The entire row uses [Modifier.semantics] with [mergeDescendants] so
 * accessibility services announce the label and the switch state together
 * as one logical control.
 *
 * @param checked         Current toggle state.
 * @param onCheckedChange Callback invoked when the user flips the switch.
 * @param modifier        Layout modifier applied to the outer row.
 */
@Composable
fun MedicationToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleDescription = stringResource(R.string.cd_medication_toggle)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = toggleDescription
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Medication,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.label_medication_toggle),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationToggleOffPreview() {
    PulseGuardTheme {
        MedicationToggle(checked = false, onCheckedChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationToggleOnPreview() {
    PulseGuardTheme {
        MedicationToggle(checked = true, onCheckedChange = {})
    }
}
