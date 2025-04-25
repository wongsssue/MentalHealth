package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(isAscending: Boolean): Flow<List<Reminder>> {
        return if (isAscending) {
            reminderDao.getAllRemindersSortedAscending()
        } else {
            reminderDao.getAllRemindersSortedDescending()
        }
    }

    suspend fun insertReminder(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }
}
