package com.example.mentalhealthemotion.Data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EmergencyContactViewModel(application: Application) : AndroidViewModel(application) {
    // Correct initialization of emergencyContactDao
    private val emergencyContactDao = AppDatabase.getDatabase(application).emergencyContactDao

    // LiveData to observe changes
    var emergencyContacts: LiveData<List<EmergencyContact>> = emergencyContactDao.getAllContacts()

    // Function to add a new contact
    fun saveContact(name: String, phoneNumber: String) {
        val contact = EmergencyContact(name = name, phoneNumber = phoneNumber)
        viewModelScope.launch {
            emergencyContactDao.insertContact(contact) // Correct method invocation
        }
    }

    // Function to delete a contact
    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            emergencyContactDao.deleteContact(contact) // Correct method invocation
            // After deletion, reload the contacts to reflect the changes
            reloadContacts()
        }
    }

    // Function to reload the contacts LiveData
    private fun reloadContacts() {
        emergencyContacts = emergencyContactDao.getAllContacts()
    }
}
