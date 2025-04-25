package com.example.mentalhealthemotion.ui.theme

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.MentalHealthViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MentalHealthScreen(
    viewModel: MentalHealthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    navController: NavController
) {
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var userInput by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf(mapOf<LocalDate, Pair<String, String>>()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val user by userViewModel.currentUser.observeAsState()
    val daysInMonth = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    val dates = (1..daysInMonth).map { LocalDate.of(selectedYear, selectedMonth, it) }
    val mentalHealthRecords by viewModel.mentalHealthRecords.observeAsState(emptyList())

    LaunchedEffect(mentalHealthRecords) {
        predictions = mentalHealthRecords.associate {
            LocalDate.parse(it.date) to (it.text to it.prediction)
        }
    }

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId -> viewModel.loadMentalHealthRecords(userId.toString()) }
    }

    LaunchedEffect(selectedDate) {
        userInput = predictions[selectedDate]?.first ?: ""
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedMonth == 1) {
                    selectedMonth = 12
                    selectedYear -= 1
                } else {
                    selectedMonth -= 1
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = "${YearMonth.of(selectedYear, selectedMonth).month.name} $selectedYear",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(8.dp)
            )

            IconButton(onClick = {
                if (selectedMonth == 12) {
                    selectedMonth = 1
                    selectedYear += 1
                } else {
                    selectedMonth += 1
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        // Calendar Grid
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(dates) { date ->
                val color = getColorByPrediction(predictions[date]?.second)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .clickable { selectedDate = date },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${date.dayOfMonth}", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Input Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Selected Date: ${selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text("Enter your thoughts") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Analyze Button
        Button(
            onClick = {
                if (userInput.isNotBlank() && user?.userID != null) {
                    coroutineScope.launch {
                        isLoading = true
                        val (sentimentLabel, _) = analyzeSentimentPrediction(userInput, "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")

                        predictions = predictions.toMutableMap().apply {
                            this[selectedDate] = userInput to sentimentLabel
                        }

                        viewModel.saveMentalHealthRecord(
                            user!!.userID.toString(),
                            selectedDate.toString(),
                            userInput,
                            sentimentLabel
                        )

                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Analyze Mood")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Sentiment
        predictions[selectedDate]?.let { (savedText, prediction) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Your Input: $savedText", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Sentiment: $prediction", style = MaterialTheme.typography.headlineSmall, color = getColorByPrediction(prediction))
                }
            }
        }

        // Emergency & Solution Buttons
        if (predictions[selectedDate]?.second in listOf("Moderate Symptoms", "Severe Symptoms", "Extreme Symptoms", "Crisis Level")) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { navController.navigate("EmergencyScreen") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Call Support", color = Color.White)
                }
                Button(
                    onClick = { navController.navigate("youtube_screen") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(Color.Blue)
                ) {
                    Text("View Solutions", color = Color.White)
                }
            }
        }
    }
}
// Suspend function for Sentiment Analysis
suspend fun analyzeSentimentPrediction(text: String, apiKey: String): Pair<String, Color> {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val requestBody = """{"inputs": "$text"}"""
        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaType(), requestBody))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@withContext "Error: Empty response" to Color.Gray
                    Log.d("SentimentDebug", "Raw API response: $responseBody")

                    val jsonArray = JSONArray(responseBody)
                    if (jsonArray.length() > 0) {
                        val sentimentArray = jsonArray.getJSONArray(0)
                        val topResult = sentimentArray.getJSONObject(0)
                        val label = topResult.getString("label") // "POSITIVE" or "NEGATIVE"
                        val score = topResult.getDouble("score")

                        val (symptomLevel, color) = when {
                            label == "POSITIVE" && score > 0.85 -> "No Symptoms" to Color.Green
                            label == "POSITIVE" -> "Mild Symptoms" to Color.Yellow
                            label == "NEGATIVE" && score > 0.95 -> "Crisis Level" to Color.Black
                            label == "NEGATIVE" && score > 0.85 -> "Extreme Symptoms" to Color.Magenta
                            label == "NEGATIVE" && score > 0.75 -> "Severe Symptoms" to Color.Red
                            label == "NEGATIVE" -> "Moderate Symptoms" to Color(0xFFFFA500) // Orange
                            else -> "Unknown" to Color.Gray
                        }
                        return@withContext "$symptomLevel" to color
                    }
                    return@withContext "Error: Unexpected response format" to Color.Gray
                } else {
                    throw IOException("Unexpected response: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("SentimentError", "Error analyzing sentiment: ${e.message}")
            return@withContext "Error analyzing sentiment" to Color.Gray
        }
    }
}
// Color Coding Function
fun getColorByPrediction(prediction: String?): Color {
    return when (prediction) {
        "No Symptoms" -> Color.Green
        "Mild Symptoms" -> Color.Yellow
        "Moderate Symptoms" -> Color(0xFFFFA500) // Orange
        "Severe Symptoms" -> Color.Red
        "Extreme Symptoms" -> Color.Magenta
        "Crisis Level" -> Color.Black
        else -> Color.Gray
    }
}
