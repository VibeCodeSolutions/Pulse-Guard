// com.example.pulseguard.ui.navigation.PulseGuardNavGraph
package com.example.pulseguard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pulseguard.ui.screens.dashboard.DashboardScreen
import com.example.pulseguard.ui.screens.entry.EntryScreen

/**
 * Root navigation graph for Pulse Guard.
 *
 * Registers all top-level destinations. [NavRoutes.DASHBOARD] is the
 * start destination. The Entry screen is reachable via the Dashboard FAB.
 * The Export route is reserved for Phase 4.
 *
 * @param navController The [NavHostController] used to drive navigation.
 *                      Defaults to a newly remembered controller so callers
 *                      do not need to create one explicitly.
 */
@Composable
fun PulseGuardNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.DASHBOARD,
    ) {
        composable(route = NavRoutes.DASHBOARD) {
            DashboardScreen(
                onAddEntry = {
                    navController.navigate(NavRoutes.ENTRY)
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
    }
}
