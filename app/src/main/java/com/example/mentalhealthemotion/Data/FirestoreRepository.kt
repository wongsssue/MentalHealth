package com.example.mentalhealthemotion.Data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    // Save Mental Health Record
    fun saveMentalHealthRecord(userId: String, date: String, text: String, prediction: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val record = MentalHealthRecord(userId, date, text, prediction)

        db.collection("mental_health_records")
            .add(record)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Retrieve Mental Health Records
    suspend fun getMentalHealthRecords(userId: String): List<MentalHealthRecord> {
        return try {
            val snapshot = db.collection("mental_health_records")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(MentalHealthRecord::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching records: ${e.message}", e)
            emptyList()
        }
    }

}
