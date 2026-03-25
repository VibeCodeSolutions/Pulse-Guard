// com.example.pulseguard.ui.navigation.PulseGuardNavGraph
package com.example.pulseguard.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pulseguard.ui.screens.dashboard.DashboardScreen
import com.example.pulseguard.ui.screens.entry.EntryScreen
import com.example.pulseguard.ui.screens.export.ExportScreen
import com.example.pulseguard.ui.screens.reminder.ReminderScreen

/**
 * Root navigation graph for Pulse Guard.
 *
 * Registers all top-level destinations. [NavRoutes.DASHBOARD] is the
 * start destination. The Entry screen is reachable via the Dashboard FAB.
 *
 * **Global Footer:** A Scaffold wraps the NavHost to provide a persistent
 * branding footer ("VibeCode Solutions") that is safely positioned below
 * any screen-level FABs or bars.
 *
 * @param navController The [NavHostController] used to drive navigation.
 */
@Composable
fun PulseGuardNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 6.dp)
                    .alpha(0.38f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VibeCode Solutions",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(route = NavRoutes.DASHBOARD) {
                DashboardScreen(
                    onAddEntry = {
                        navController.navigate(NavRoutes.ENTRY)
                    },
                    onExport = {
                        navController.navigate(NavRoutes.EXPORT)
                    },
                    onReminders = {
                        navController.navigate(NavRoutes.REMINDER)
                    },
                )
            }

            composable(route = NavRoutes.ENTRY) {
                EntryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable(route = NavRoutes.EXPORT) {
                ExportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable(route = NavRoutes.REMINDER) {
                ReminderScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}
