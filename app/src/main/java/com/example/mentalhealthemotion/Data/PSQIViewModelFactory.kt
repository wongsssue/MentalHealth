package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PSQIViewModelFactory(private val repository: PSQIRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PSQIVIewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PSQIVIewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
