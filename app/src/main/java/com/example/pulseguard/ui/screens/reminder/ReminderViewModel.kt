// com.example.pulseguard.ui.screens.reminder.ReminderViewModel
package com.example.pulseguard.ui.screens.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.data.local.entity.daysSet
import com.example.pulseguard.data.local.entity.encodeDays
import com.example.pulseguard.domain.usecase.DeleteReminderUseCase
import com.example.pulseguard.domain.usecase.GetRemindersUseCase
import com.example.pulseguard.domain.usecase.SaveReminderUseCase
import com.example.pulseguard.domain.usecase.ToggleReminderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Reminder screen.
 *
 * Manages the list of reminders and the add/edit dialog state.
 * All mutations go through [onEvent] to maintain a strict unidirectional
 * data flow.
 */
class ReminderViewModel(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val toggleReminderUseCase: ToggleReminderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())

    /** Observable UI state exposed to the Reminder screen. */
    val uiState: StateFlow<ReminderUiState> = _uiState

    init {
        viewModelScope.launch {
            getRemindersUseCase.observe().collect { reminders ->
                _uiState.update {
                    it.copy(reminders = reminders, isLoading = false)
                }
            }
        }
    }

    /** Dispatches a [ReminderEvent] to update state or trigger side effects. */
    fun onEvent(event: ReminderEvent) {
        when (event) {
            is ReminderEvent.AddClicked -> openNewDialog()
            is ReminderEvent.EditClicked -> openEditDialog(event.reminder)
            is ReminderEvent.DeleteClicked -> handleDelete(event.id)
            is ReminderEvent.ToggleClicked ->
                handleToggle(event.id, event.enabled)
            is ReminderEvent.DialogLabelChanged ->
                _uiState.update { it.copy(dialogLabel = event.label) }
            is ReminderEvent.DialogTypeChanged ->
                _uiState.update { it.copy(dialogType = event.type) }
            is ReminderEvent.DialogTimeChanged ->
                _uiState.update {
                    it.copy(dialogHour = event.hour, dialogMinute = event.minute)
                }
            is ReminderEvent.DialogDayToggled -> toggleDay(event)
            is ReminderEvent.DialogSave -> handleSave()
            is ReminderEvent.DialogDismiss -> closeDialog()
            is ReminderEvent.PermissionResult ->
                _uiState.update {
                    it.copy(hasNotificationPermission = event.granted)
                }
        }
    }

    private fun openNewDialog() {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingReminderId = null,
                dialogLabel = "",
                dialogType = com.example.pulseguard.domain.model.ReminderType.MEDICATION,
                dialogHour = 8,
                dialogMinute = 0,
                dialogDays = java.time.DayOfWeek.entries.toSet(),
            )
        }
    }

    private fun openEditDialog(reminder: Reminder) {
        _uiState.update {
            it.copy(
                showDialog = true,
                editingReminderId = reminder.id,
                dialogLabel = reminder.label,
                dialogType = reminder.type,
                dialogHour = reminder.hour,
                dialogMinute = reminder.minute,
                dialogDays = reminder.daysSet,
            )
        }
    }

    private fun toggleDay(event: ReminderEvent.DialogDayToggled) {
        _uiState.update { state ->
            val days = state.dialogDays.toMutableSet()
            if (event.day in days) {
                // Prevent deselecting the last day.
                if (days.size > 1) days.remove(event.day)
            } else {
                days.add(event.day)
            }
            state.copy(dialogDays = days)
        }
    }

    private fun handleSave() {
        val state = _uiState.value
        if (!state.isDialogSaveEnabled) return

        val reminder = Reminder(
            id = state.editingReminderId ?: 0,
            label = state.dialogLabel.trim(),
            type = state.dialogType,
            hour = state.dialogHour,
            minute = state.dialogMinute,
            days = encodeDays(state.dialogDays),
        )
        viewModelScope.launch {
            saveReminderUseCase.execute(reminder)
        }
        closeDialog()
    }

    private fun handleDelete(id: Long) {
        viewModelScope.launch {
            deleteReminderUseCase.execute(id)
        }
    }

    private fun handleToggle(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            toggleReminderUseCase.execute(id, enabled)
        }
    }

    private fun closeDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }
}
