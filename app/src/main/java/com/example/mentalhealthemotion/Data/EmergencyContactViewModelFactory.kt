package com.example.mentalhealthemotion.Data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EmergencyContactViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyContactViewModel::class.java)) {
            return EmergencyContactViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
