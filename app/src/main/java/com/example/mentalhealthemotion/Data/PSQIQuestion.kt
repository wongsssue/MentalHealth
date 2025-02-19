package com.example.mentalhealthemotion.Data

data class PSQIQuestion(
    val id: Int,
    val question: String,
    val type: QuestionType
)

data class PSQIResponse(
    val questionId: Int,
    val answer: Any // Can be String, Int, or Double depending on the question
)

sealed class QuestionType {
    data class Objective(val options: List<String>) : QuestionType()
    object Subjective : QuestionType()
}

