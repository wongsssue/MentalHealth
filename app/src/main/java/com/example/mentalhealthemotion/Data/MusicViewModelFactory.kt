package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MusicViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
