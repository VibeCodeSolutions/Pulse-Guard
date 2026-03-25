// com.example.pulseguard.ui.screens.reminder.ReminderUiState
package com.example.pulseguard.ui.screens.reminder

import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.domain.model.ReminderType
import java.time.DayOfWeek

/**
 * Immutable UI state for the Reminder screen.
 *
 * @property reminders               All stored reminders, ordered by time.
 * @property isLoading               True while the initial data load is in progress.
 * @property showDialog              True when the add/edit dialog should be visible.
 * @property editingReminderId       Non-null when editing an existing reminder.
 * @property dialogLabel             Current label value in the dialog.
 * @property dialogType              Current reminder type in the dialog.
 * @property dialogHour              Current hour in the dialog (0–23).
 * @property dialogMinute            Current minute in the dialog (0–59).
 * @property dialogDays              Currently selected days in the dialog.
 * @property hasNotificationPermission Whether POST_NOTIFICATIONS is granted.
 */
data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true,
    val showDialog: Boolean = false,
    val editingReminderId: Long? = null,
    val dialogLabel: String = "",
    val dialogType: ReminderType = ReminderType.MEDICATION,
    val dialogHour: Int = 8,
    val dialogMinute: Int = 0,
    val dialogDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val hasNotificationPermission: Boolean = false,
) {
    /** True when the dialog has enough data to save. */
    val isDialogSaveEnabled: Boolean
        get() = dialogLabel.isNotBlank() && dialogDays.isNotEmpty()
}
