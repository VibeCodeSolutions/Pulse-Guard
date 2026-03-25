// com.example.pulseguard.ui.navigation.NavRoutes
package com.example.pulseguard.ui.navigation

/**
 * Centralised route strings for the Pulse Guard navigation graph.
 *
 * Using string constants prevents typos and enables safe refactoring.
 * New destinations should be added here before being registered in
 * [PulseGuardNavGraph].
 */
object NavRoutes {
    /** Main dashboard overview screen (startDestination). */
    const val DASHBOARD = "dashboard"

    /** Blood pressure entry / data-capture screen. */
    const val ENTRY = "entry"

    /** PDF export screen (Phase 4). */
    const val EXPORT = "export"

    /** Reminder configuration screen (Phase 8). */
    const val REMINDER = "reminder"
}
