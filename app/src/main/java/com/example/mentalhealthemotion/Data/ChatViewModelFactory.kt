package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log

class ChatViewModelFactory() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel() as T // Pass chatRepository correctly
        }
        Log.e("ChatViewModelFactory", "Unknown ViewModel class: ${modelClass.simpleName}")
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
