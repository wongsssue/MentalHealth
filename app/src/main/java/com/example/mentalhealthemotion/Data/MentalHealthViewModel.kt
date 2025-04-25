package com.example.mentalhealthemotion.Data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentalhealthemotion.Data.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MentalHealthViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _mentalHealthRecords = MutableLiveData<List<MentalHealthRecord>>()
    val mentalHealthRecords: LiveData<List<MentalHealthRecord>> get() = _mentalHealthRecords


    fun saveMentalHealthRecord(userId: String, date: String, text: String, prediction: String) {
        val record = hashMapOf(
            "userId" to userId,
            "date" to date,
            "text" to text,
            "prediction" to prediction
        )

        db.collection("mental_health_records")
            .add(record)
            .addOnSuccessListener {
                Log.d("MentalHealthViewModel", "Record saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("MentalHealthViewModel", "Error saving record", e)
            }
    }



    fun loadMentalHealthRecords(userId: String) {
        viewModelScope.launch {
            try {
                db.collection("mental_health_records")
                    .whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val records = documents.map { it.toObject(MentalHealthRecord::class.java) }
                        _mentalHealthRecords.postValue(records)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MentalHealthViewModel", "Error fetching records", e)
                    }
            } catch (e: Exception) {
                Log.e("MentalHealthViewModel", "Error loading records: ${e.message}", e)
            }
        }
    }



}

