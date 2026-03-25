// com.example.pulseguard.ui.screens.reminder.ReminderEvent
package com.example.pulseguard.ui.screens.reminder

import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.domain.model.ReminderType
import java.time.DayOfWeek

/**
 * Sealed interface for user-initiated events on the Reminder screen.
 */
sealed interface ReminderEvent {

    /** User tapped the FAB to add a new reminder. */
    data object AddClicked : ReminderEvent

    /** User tapped an existing reminder to edit it. */
    data class EditClicked(val reminder: Reminder) : ReminderEvent

    /** User requested deletion of a reminder. */
    data class DeleteClicked(val id: Long) : ReminderEvent

    /** User toggled the enabled switch on a reminder. */
    data class ToggleClicked(val id: Long, val enabled: Boolean) : ReminderEvent

    /** User changed the label in the add/edit dialog. */
    data class DialogLabelChanged(val label: String) : ReminderEvent

    /** User changed the reminder type in the dialog. */
    data class DialogTypeChanged(val type: ReminderType) : ReminderEvent

    /** User changed the time in the dialog. */
    data class DialogTimeChanged(val hour: Int, val minute: Int) : ReminderEvent

    /** User toggled a day of week in the dialog. */
    data class DialogDayToggled(val day: DayOfWeek) : ReminderEvent

    /** User confirmed save in the add/edit dialog. */
    data object DialogSave : ReminderEvent

    /** User dismissed the add/edit dialog. */
    data object DialogDismiss : ReminderEvent

    /** Result of the POST_NOTIFICATIONS permission request. */
    data class PermissionResult(val granted: Boolean) : ReminderEvent
}
