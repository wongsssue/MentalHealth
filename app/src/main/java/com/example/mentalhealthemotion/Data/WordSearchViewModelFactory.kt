package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mentalhealthemotion.Data.WordSearchRepository

class WordSearchViewModelFactory(private val repository: WordSearchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordSearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
