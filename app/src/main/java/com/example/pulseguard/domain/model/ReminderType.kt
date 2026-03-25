// com.example.pulseguard.domain.model.ReminderType
package com.example.pulseguard.domain.model

/**
 * Type of reminder notification a user can configure.
 */
enum class ReminderType {
    /** Reminder to take medication. */
    MEDICATION,

    /** Reminder to measure blood pressure. */
    MEASUREMENT,
}
