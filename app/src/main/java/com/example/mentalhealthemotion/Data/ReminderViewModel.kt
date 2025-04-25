package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {

    // Default to ascending order
    var reminders: LiveData<List<Reminder>> = reminderRepository.getAllReminders(true).asLiveData()

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.insertReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(reminder)
        }
    }

    // Function to update the reminder list with sorting order
    fun setSortingOrder(isAscending: Boolean) {
        reminders = reminderRepository.getAllReminders(isAscending).asLiveData()
    }
}