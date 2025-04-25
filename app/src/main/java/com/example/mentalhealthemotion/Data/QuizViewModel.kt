package com.example.mentalhealthemotion.Data

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class QuizViewModel : ViewModel() {
    private var isResultSaved = false

    fun saveQuizResultOnce(userId: String, score: Int, severity: String) {
        if (!isResultSaved) {
            saveQuizResult(userId, score, severity)
            isResultSaved = true
        }
    }

    fun saveDASSQuizResultOnce(
        userId: String,
        depressionScore: Int, anxietyScore: Int, stressScore: Int,
        depressionSeverity: String, anxietySeverity: String, stressSeverity: String
    ) {
        if (!isResultSaved) {
            saveDASSQuizResult(userId, depressionScore, anxietyScore, stressScore, depressionSeverity, anxietySeverity, stressSeverity)
            isResultSaved = true
        }
    }


    private val firestore = FirebaseFirestore.getInstance()

    // Function to load quiz results from Firestore
    fun loadQuiz(userId: String, onComplete: () -> Unit) {
        firestore.collection("PHQ9_Results")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Handle loaded data if needed
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                println("Error loading quiz data: $e")
            }
    }

    fun saveQuizResult(userId: String, score: Int, severity: String) {
        val assessmentResult = hashMapOf(
            "userId" to userId,
            "score" to score,
            "severity" to severity,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("PHQ9_Results")
            .add(assessmentResult)
            .addOnSuccessListener { println("Data saved successfully!") }
            .addOnFailureListener { e -> println("Error saving data: $e") }
    }


    // Function to load quiz results from Firestore
    fun loadDASSQuiz(userId: String, onComplete: () -> Unit) {
        firestore.collection("DASS_Results")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Handle loaded data if needed
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                println("Error loading quiz data: $e")
            }
    }

    fun saveDASSQuizResult(
        userId: String,
        depressionScore: Int, anxietyScore: Int, stressScore: Int,
        depressionSeverity: String, anxietySeverity: String, stressSeverity: String
    ) {
        val assessmentResult = hashMapOf(
            "userId" to userId,
            "depressionScore" to depressionScore,
            "anxietyScore" to anxietyScore,
            "stressScore" to stressScore,
            "depressionSeverity" to depressionSeverity,
            "anxietySeverity" to anxietySeverity,
            "stressSeverity" to stressSeverity,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("DASS_Results")
            .add(assessmentResult)
            .addOnSuccessListener { println("DASS data saved successfully!") }
            .addOnFailureListener { e -> println("Error saving DASS data: $e") }
    }


}
