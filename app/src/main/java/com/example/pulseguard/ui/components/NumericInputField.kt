// com.example.pulseguard.ui.components.NumericInputField
package com.example.pulseguard.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.pulseguard.ui.theme.PulseGuardTheme

/**
 * Reusable numeric-only input field built on top of [OutlinedTextField].
 *
 * Enforces [KeyboardType.Number] so the number pad remains visible throughout
 * the entire data-entry flow. Only digit characters are accepted; the input is
 * additionally limited to [maxLength] characters.
 *
 * @param value          Current text value from state.
 * @param onValueChange  Callback invoked with the filtered new value.
 * @param label          Field label shown inside the outline.
 * @param modifier       Layout modifier applied to the text field.
 * @param errorMessage   When non-null, the field is highlighted in error colour
 *                       and this message is shown as supporting text.
 * @param imeAction      IME action displayed on the keyboard (default [ImeAction.Next]).
 * @param keyboardActions Keyboard action callbacks (e.g. move focus on Next).
 * @param focusRequester [FocusRequester] that can programmatically request focus.
 * @param maxLength      Maximum number of digits accepted (default 3).
 */
@Composable
fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester = remember { FocusRequester() },
    maxLength: Int = 3,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Accept only digit characters up to maxLength
            if (input.length <= maxLength && input.all { it.isDigit() }) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        modifier = modifier.focusRequester(focusRequester),
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) {
            { Text(errorMessage) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        singleLine = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun NumericInputFieldPreview() {
    PulseGuardTheme {
        NumericInputField(
            value = "120",
            onValueChange = {},
            label = "Systolisch (mmHg)",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NumericInputFieldErrorPreview() {
    PulseGuardTheme {
        NumericInputField(
            value = "999",
            onValueChange = {},
            label = "Systolisch (mmHg)",
            modifier = Modifier.fillMaxWidth(),
            errorMessage = "Wert muss zwischen 60 und 300 mmHg liegen",
        )
    }
}
