package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentalhealthemotion.API.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class QuestionnaireViewModel : ViewModel() {

    private val _questions = MutableStateFlow<List<String>>(emptyList())
    val questions: StateFlow<List<String>> = _questions

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    init {
        fetchQuestions()
    }

    private fun fetchQuestions() {
        viewModelScope.launch {
            try {
                val fetchedQuestions = RetrofitInstance.api.getQuestions()
                _questions.value = fetchedQuestions
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitAnswers(answers: List<String?>) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.submitAnswers(answers)
                _result.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}