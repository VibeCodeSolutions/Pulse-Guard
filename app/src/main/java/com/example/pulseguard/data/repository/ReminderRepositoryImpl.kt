// com.example.pulseguard.data.repository.ReminderRepositoryImpl
package com.example.pulseguard.data.repository

import com.example.pulseguard.data.local.dao.ReminderDao
import com.example.pulseguard.data.local.entity.Reminder
import com.example.pulseguard.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [ReminderRepository] backed by Room via [ReminderDao].
 */
class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders()

    override suspend fun getEnabledReminders(): List<Reminder> =
        reminderDao.getEnabledReminders()

    override suspend fun getReminderById(id: Long): Reminder? =
        reminderDao.getReminderById(id)

    override suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder)

    override suspend fun deleteReminder(id: Long) {
        reminderDao.deleteReminder(id)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        reminderDao.setEnabled(id, enabled)
    }
}
