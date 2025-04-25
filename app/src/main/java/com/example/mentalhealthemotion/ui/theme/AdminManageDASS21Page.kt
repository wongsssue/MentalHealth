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
fun AdminManageDASS21Page() {
    val resultsState = remember { mutableStateOf<Map<String, List<DASSResult>>>(emptyMap()) }

// Load DASS results from Firestore
    LaunchedEffect(Unit) {
        loadDASSQuizResults { results ->
            resultsState.value = results.groupBy { it.userId } // Group by userId
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("DASS21 Results", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (resultsState.value.isEmpty()) {
            Text("No results available.")
        } else {
            LazyColumn {
                resultsState.value.forEach { (userId, results) ->
                    item {
                        Text("User ID: $userId", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    items(results) { result ->
                        DASSResultItem(result) {
                            deleteDASSResult(result)
                            resultsState.value = resultsState.value.toMutableMap().apply {
                                this[userId] = this[userId]?.filterNot { it == result } ?: emptyList()
                                if (this[userId]?.isEmpty() == true) remove(userId)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DASSResultItem(result: DASSResult, onDelete: () -> Unit) {
    val formattedTime = remember(result.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(result.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Depression: ${result.depressionScore} (${result.depressionSeverity})")
            Text("Anxiety: ${result.anxietyScore} (${result.anxietySeverity})")
            Text("Stress: ${result.stressScore} (${result.stressSeverity})")
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


fun deleteDASSResult(result: DASSResult) {
    val db = FirebaseFirestore.getInstance()
    db.collection("DASS_Results")
        .whereEqualTo("userId", result.userId)
        .whereEqualTo("depressionScore", result.depressionScore)
        .whereEqualTo("anxietyScore", result.anxietyScore)
        .whereEqualTo("stressScore", result.stressScore)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                db.collection("DASS_Results").document(document.id).delete()
                    .addOnSuccessListener { println("Deleted successfully") }
                    .addOnFailureListener { e -> println("Error deleting: $e") }
            }
        }
        .addOnFailureListener { e -> println("Error finding document: $e") }
}


data class DASSResult(
    val userId: String,
    val depressionScore: Int,
    val depressionSeverity: String,
    val anxietyScore: Int,
    val anxietySeverity: String,
    val stressScore: Int,
    val stressSeverity: String,
    val timestamp: Long // Store as a Long (milliseconds)
)


fun loadDASSQuizResults(onComplete: (List<DASSResult>) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("DASS_Results")
        .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by latest first
        .get()
        .addOnSuccessListener { documents ->
            val results = documents.map { doc ->
                DASSResult(
                    doc.getString("userId") ?: "",
                    doc.getLong("depressionScore")?.toInt() ?: 0,
                    doc.getString("depressionSeverity") ?: "",
                    doc.getLong("anxietyScore")?.toInt() ?: 0,
                    doc.getString("anxietySeverity") ?: "",
                    doc.getLong("stressScore")?.toInt() ?: 0,
                    doc.getString("stressSeverity") ?: "",
                    doc.getLong("timestamp") ?: 0L // Get timestamp
                )
            }
            onComplete(results)
        }
        .addOnFailureListener { e ->
            println("Error loading DASS data: $e")
            onComplete(emptyList())
        }
}


