package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CommunityViewModelFactory(private val repository: CommunityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommunityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
