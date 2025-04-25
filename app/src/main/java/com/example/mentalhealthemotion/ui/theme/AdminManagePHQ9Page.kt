package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminManagePHQ9Page() {
    val resultsState = remember { mutableStateOf<Map<String, List<PHQ9Result>>>(emptyMap()) }

    // Load PHQ9 results from Firestore
    LaunchedEffect(Unit) {
        loadPHQ9QuizResults { results ->
            // Group results by userId
            val groupedResults = results.groupBy { it.userId }
            resultsState.value = groupedResults
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("PHQ9 Results", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (resultsState.value.isEmpty()) {
            Text("No results available.")
        } else {
            LazyColumn {
                resultsState.value.forEach { (userId, results) ->
                    item {
                        Text("User ID: $userId", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    items(results) { result ->
                        PHQ9ResultItem(result) {
                            deletePHQ9Result(result) {
                                // Refresh results after deletion
                                loadPHQ9QuizResults { updatedResults ->
                                    resultsState.value = updatedResults.groupBy { it.userId }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PHQ9ResultItem(result: PHQ9Result, onDelete: () -> Unit) {
    val formattedTime = remember(result.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(result.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Score: ${result.score} (${result.severity})")
            Text("Submitted: $formattedTime", fontStyle = FontStyle.Italic)

            Button(
                onClick = { onDelete() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Delete", color = Color.White)
            }
        }
    }
}


// Function to load PHQ9 results from Firestore
fun loadPHQ9QuizResults(onComplete: (List<PHQ9Result>) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("PHQ9_Results")
        .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by latest first
        .get()
        .addOnSuccessListener { documents ->
            val results = documents.map { doc ->
                PHQ9Result(
                    doc.getString("userId") ?: "",
                    doc.getLong("score")?.toInt() ?: 0,
                    doc.getString("severity") ?: "",
                    doc.getLong("timestamp") ?: 0L // Get timestamp
                )
            }
            onComplete(results)
        }
        .addOnFailureListener { e ->
            println("Error loading PHQ9 data: $e")
            onComplete(emptyList())
        }
}

// Function to delete a PHQ9 result from Firestore
fun deletePHQ9Result(result: PHQ9Result, onComplete: () -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("PHQ9_Results")
        .whereEqualTo("userId", result.userId)
        .whereEqualTo("timestamp", result.timestamp) // Ensure correct deletion
        .get()
        .addOnSuccessListener { documents ->
            for (doc in documents) {
                doc.reference.delete()
            }
            onComplete()
        }
        .addOnFailureListener { e ->
            println("Error deleting PHQ9 result: $e")
        }
}


data class PHQ9Result(
    val userId: String,
    val score: Int,
    val severity: String,
    val timestamp: Long // Store submission time as a Long (milliseconds)
)
