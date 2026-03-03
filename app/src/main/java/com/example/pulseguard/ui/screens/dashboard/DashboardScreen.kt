// com.example.pulseguard.ui.screens.dashboard.DashboardScreen
package com.example.pulseguard.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pulseguard.R
import com.example.pulseguard.ui.theme.PulseGuardTheme
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Placeholder Dashboard screen for Phase 2.
 *
 * The full implementation (period selector, aggregation, chart, list) will be
 * added by ADA in Phase 3. This stub provides the [startDestination] for the
 * nav graph and exposes the FAB for navigating to the Entry screen.
 *
 * @param onAddEntry Lambda invoked when the user taps the FAB to add a new entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddEntry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_placeholder_title)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.cd_add_entry),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.dashboard_placeholder_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    PulseGuardTheme {
        DashboardScreen(onAddEntry = {})
    }
}
