// com.example.pulseguard.ui.components.ArmSelector
package com.example.pulseguard.ui.components

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.pulseguard.R
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.ui.theme.PulseGuardTheme

private val ARMS = listOf(MeasurementArm.LEFT, MeasurementArm.RIGHT)

/**
 * Segmented button row for selecting the measurement arm (Left / Right).
 *
 * Uses [SingleChoiceSegmentedButtonRow] from Material3. Touch targets are
 * at least 48 dp tall by default (M3 specification).
 *
 * @param selectedArm  Currently active arm selection.
 * @param onArmSelected Callback invoked when the user taps a segment.
 * @param modifier      Layout modifier applied to the row.
 */
@Composable
fun ArmSelector(
    selectedArm: MeasurementArm,
    onArmSelected: (MeasurementArm) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        stringResource(R.string.label_arm_left),
        stringResource(R.string.label_arm_right),
    )
    val selectorDescription = stringResource(R.string.cd_arm_selector)

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.semantics { contentDescription = selectorDescription },
    ) {
        ARMS.forEachIndexed { index, arm ->
            SegmentedButton(
                selected = selectedArm == arm,
                onClick = { onArmSelected(arm) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ARMS.size),
                label = { Text(labels[index]) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArmSelectorPreview() {
    PulseGuardTheme {
        ArmSelector(
            selectedArm = MeasurementArm.LEFT,
            onArmSelected = {},
        )
    }
}
