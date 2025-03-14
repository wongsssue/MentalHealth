package com.example.mentalhealthemotion.Data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MoodEntryRepository(
    private val moodEntryDao: MoodEntryDao,
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val moodEntriesCollection = firestore.collection("moodEntries")

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun insertMoodEntry(userId: Int, moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                moodEntriesCollection
                    .document(userId.toString())  // Store under user document
                    .collection("moodEntries")
                    .document(moodEntry.moodEntryID.toString())
                    .set(moodEntry)
                    .await()
                Log.d("MoodEntryRepository", "Mood entry saved under user $userId in Firestore.")
            }
            moodEntryDao.insertMoodEntry(moodEntry)  // Save locally with Room
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to save mood entry: ${e.message}")
        }
    }

    suspend fun updateMoodEntry(userId: Int, moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val documentRef = moodEntriesCollection
                    .document(userId.toString()) // Reference the user document
                    .collection("moodEntries")  // Subcollection for mood entries
                    .document(moodEntry.moodEntryID.toString()) // Reference the specific mood entry

                documentRef.set(moodEntry).await()
                Log.d("MoodEntryRepository", "Mood entry updated in Firestore for user $userId.")
            }
            moodEntryDao.updateMoodEntry(moodEntry)
            Log.d("MoodEntryRepository", "Mood entry updated in Room for user $userId.")
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to update mood entry for user $userId: ${e.message}")
        }
    }

    suspend fun deleteMoodEntry(userId: Int, moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val documentRef = moodEntriesCollection
                    .document(userId.toString()) // Reference the user document
                    .collection("moodEntries")  // Reference the mood entries subcollection
                    .document(moodEntry.moodEntryID.toString()) // Reference the specific mood entry

                documentRef.delete().await()
                Log.d("MoodEntryRepository", "Mood entry deleted from Firestore for user $userId.")
            }
            moodEntryDao.deleteMoodEntry(moodEntry)
            Log.d("MoodEntryRepository", "Mood entry deleted from Room for user $userId.")
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to delete mood entry for user $userId: ${e.message}")
        }
    }


    suspend fun loadMoodEntries(userId: Int): List<MoodEntry> {
        return try {
            if (isOnline()) {
                val snapshot = moodEntriesCollection
                    .document(userId.toString())
                    .collection("moodEntries")
                    .get()
                    .await()
                val moodEntries = snapshot.documents.mapNotNull { it.toObject(MoodEntry::class.java) }
                Log.d("MoodEntryRepository", "Fetched ${moodEntries.size} mood entries from Firestore.")
                moodEntries
            } else {
                val localEntries = moodEntryDao.getAllMoodEntriesByUser(userId)
                Log.d("MoodEntryRepository", "Fetched ${localEntries.size} mood entries from Room (offline).")
                localEntries
            }
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to load mood entries: ${e.message}")
            emptyList()
        }
    }


    // Function to count moods for the current month
    suspend fun countMoodsForCurrentMonth(userId: Int): List<MoodCount> {
        return withContext(Dispatchers.IO) {
            try {
                if (isOnline()) {
                    val currentMonthYear = getCurrentMonthYear()
                    Log.d("MoodEntryRepository", "Current Month-Year: $currentMonthYear")

                    val snapshot = moodEntriesCollection
                        .document(userId.toString())
                        .collection("moodEntries")
                        .get()
                        .await()

                    val filteredMoods = snapshot.documents
                        .mapNotNull { it.toObject(MoodEntry::class.java) }
                        .filter { it.date?.substring(3, 10) == currentMonthYear }
                        .groupingBy { it.moodType }
                        .eachCount()

                    val allMoodCounts = MoodType.values().map { moodType ->
                        MoodCount(moodType.name, filteredMoods[moodType] ?: 0)
                    }

                    Log.d("MoodEntryRepository", "Fetched ${allMoodCounts.size} mood counts from Firestore.")
                    return@withContext allMoodCounts
                } else {
                    throw Exception("Offline mode")
                }
            } catch (e: Exception) {
                Log.e("MoodEntryRepository", "Firestore fetch failed: ${e.message}, falling back to Room.")

                val currentMonthYear = getCurrentMonthYear()
                val localCounts = moodEntryDao.countMoodsByMonthYear(userId, currentMonthYear)

                val allMoodCounts = MoodType.values().map { moodType ->
                    MoodCount(moodType.name, localCounts.find { it.moodType == moodType.name }?.count ?: 0)
                }

                Log.d("MoodEntryRepository", "Fetched ${allMoodCounts.size} mood counts from Room (offline).")
                return@withContext allMoodCounts
            }
        }
    }

    private fun getCurrentMonthYear(): String {
        val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        return dateFormat.format(Date())
    }


    fun getMoodActivityDataForMonth(userId: Int): Flow<Map<MoodType, List<ActivityPercentage>>> {
        val monthYear = getCurrentMonthYear()

        return flow {
            try {
                if (isOnline()) {
                    // Fetch from Firestore
                    val snapshot = moodEntriesCollection
                        .document(userId.toString())
                        .collection("moodEntries")
                        .get()
                        .await()

                    val moodEntries = snapshot.documents
                        .mapNotNull { it.toObject(MoodEntry::class.java) }
                        .filter { it.date.orEmpty().contains(monthYear) } // Filter by current month-year

                    val moodDataMap = moodEntries.groupBy { it.moodType }.mapValues { (_, entries) ->
                        val totalMoodDays = entries.size.toDouble()
                        val activityCounts = entries.flatMap { it.activityName }
                            .groupingBy { it }
                            .eachCount()

                        activityCounts.map { (activity, count) ->
                            ActivityPercentage(activity, (count / totalMoodDays) * 100)
                        }
                    }

                    emit(moodDataMap) // Emit Firestore data
                } else {
                    // Fetch from Room Database (Must be collected)
                    moodEntryDao.getActivitiesForMoodInMonth(userId, monthYear)
                        .collect { offlineEntries ->
                            val moodDataMap = offlineEntries.groupBy { it.moodType }.mapValues { (_, entries) ->
                                val totalMoodDays = entries.size.toDouble()
                                val activityCounts = entries.flatMap { it.activityName }
                                    .groupingBy { it }
                                    .eachCount()

                                activityCounts.map { (activity, count) ->
                                    ActivityPercentage(activity, (count / totalMoodDays) * 100)
                                }
                            }
                            emit(moodDataMap) // Emit Room database data
                        }
                }
            } catch (e: Exception) {
                Log.e("MoodRepository", "Failed to fetch data: ${e.message}")
                emit(emptyMap()) // Emit empty map in case of error
            }
        }.flowOn(Dispatchers.IO) // Run on background thread
    }

    fun getCurrentWeekDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val startOfWeek = dateFormat.format(calendar.time)

        // Move to Sunday
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = dateFormat.format(calendar.time)

        return startOfWeek to endOfWeek
    }

    fun getMoodsForCurrentWeek(userId: Int): Flow<Map<MoodType, List<MoodCountByDay>>> = flow {
        val (startDate, endDate) = getCurrentWeekDateRange() // Get Monday-Sunday range

        try {
            if (isOnline()) {
                val snapshot = moodEntriesCollection
                    .document(userId.toString())
                    .collection("moodEntries")
                    .get()
                    .await()

                // Extract data from Firestore and filter by date
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dayFormatter = SimpleDateFormat("dd", Locale.getDefault()) // Extracts day as "01", "02", etc.

                val filteredMoods = snapshot.documents
                    .mapNotNull { it.toObject(MoodEntry::class.java) }
                    .filter { moodEntry ->
                        moodEntry.date?.let { dateStr ->
                            try {
                                val parsedDate = formatter.parse(dateStr) ?: return@filter false
                                val startDateParsed = formatter.parse("$startDate 00:00:00")
                                val endDateParsed = formatter.parse("$endDate 23:59:59.999")

                                parsedDate in startDateParsed..endDateParsed
                            } catch (e: Exception) {
                                false // If parsing fails, exclude this entry
                            }
                        } ?: false // If date is null, exclude it
                    }
                    .groupBy { moodEntry ->
                        moodEntry.date?.let { dateStr -> dayFormatter.format(formatter.parse(dateStr)!!) } ?: "Unknown"
                    }

                val firestoreData = filteredMoods.flatMap { (day, moods) ->
                    moods.groupingBy { it.moodType }.eachCount().map { (moodType, count) ->
                        MoodCountByDay(day, moodType ?: MoodType.meh, count)
                    }
                }.groupBy { it.moodType } // Ensure the grouping is by MoodType

                Log.d("MoodEntryRepository", "Fetched weekly mood counts from Firestore.")
                emit(firestoreData)
                return@flow
            } else {
                throw Exception("Offline mode")
            }
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Firestore fetch failed: ${e.message}, falling back to Room.")
        }

        // Fetch from Room database (Offline Mode)
        val weeklyCounts = moodEntryDao.countMoodsByWeek(userId, startDate, endDate)
            .map { moodList ->
                moodList.groupBy { moodEntry ->
                    moodEntry.moodType ?: MoodType.meh
                }.mapValues { (_, counts) ->
                    counts.map { MoodCountByDay(it.day, it.moodType, it.mood_count) } // Use correct `day` field
                }
            }

        Log.d("MoodEntryRepository", "Fetched weekly mood counts from Room (offline).")
        emitAll(weeklyCounts)
    }.flowOn(Dispatchers.IO)

    //face emotion detection
    private val apiKey = "7uLxaOq7gDj6tkExACp7CeauUb6HV0gP"
    private val apiSecret = "80afFCbeGyRgJ_ImECE_LWW_GrqeajoR"
    private val url = "https://api-us.faceplusplus.com/facepp/v3/detect"
    private val client = OkHttpClient()

    suspend fun detectEmotion(context: Context, imageUri: Uri): MoodType {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes == null) return@withContext MoodType.meh

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", apiKey)
                    .addFormDataPart("api_secret", apiSecret)
                    .addFormDataPart("return_attributes", "emotion")
                    .addFormDataPart(
                        "image_file", "uploaded_image.jpg",
                        RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
                    )
                    .build()

                val request = Request.Builder().url(url).post(requestBody).build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "{}"
                extractMoodFromResponse(responseBody)

            } catch (e: Exception) {
                MoodType.meh
            }
        }
    }


    private fun extractMoodFromResponse(response: String): MoodType {
        val jsonObject = JSONObject(response)
        val emotionObj = jsonObject.optJSONArray("faces")?.optJSONObject(0)
            ?.optJSONObject("attributes")?.optJSONObject("emotion")

        return if (emotionObj != null) {
            val happiness = emotionObj.optDouble("happiness", 0.0)
            val anger = emotionObj.optDouble("anger", 0.0)
            val neutral = emotionObj.optDouble("neutral", 0.0)
            val sadness = emotionObj.optDouble("sadness", 0.0)
            val disgust = emotionObj.optDouble("disgust", 0.0)
            val surprise = emotionObj.optDouble("surprise", 0.0)
            val fear = emotionObj.optDouble("fear", 0.0)
            Log.d("EmotionValues", "Happiness: $happiness, Anger: $anger, Neutral: $neutral, Sadness: $sadness, Disgust: $disgust, Surprise: $surprise, Fear: $fear")

            when {
                happiness > 65 -> MoodType.rad
                happiness > 50 -> MoodType.good
                anger > 20 -> MoodType.awful
                neutral > 60 -> MoodType.meh
                disgust > 10 -> MoodType.bad
                sadness > 20 || (sadness > 10 && neutral < 80) -> MoodType.bad
                surprise > 50 -> MoodType.good
                fear > 35 -> MoodType.bad
                else -> MoodType.meh
            }
        } else {
            MoodType.meh
        }
    }

    private val apiSentimentAnalysisKey = "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"
    private val urlSentimentAnalysis = "https://api-inference.huggingface.co/models/cardiffnlp/twitter-roberta-base-sentiment"
    private val clientSentimentAnalysis = OkHttpClient()

    fun analyzeSentiment(userNote: String, callback: (String) -> Unit) {
        val json = """{"inputs": "$userNote"}"""
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(urlSentimentAnalysis)
            .addHeader("Authorization", "Bearer $apiSentimentAnalysisKey")
            .post(requestBody)
            .build()

        clientSentimentAnalysis.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val mood = parseAndMapSentiment(responseBody)
                    callback(mood)
                }
            }
        })
    }

    private fun parseAndMapSentiment(response: String): String {
        try {
            Log.d("SentimentResponse", "Raw API Response: $response")

            // Parse outermost array first
            val outerArray = JSONArray(response)
            if (outerArray.length() == 0) return "Unknown Mood"

            // Extract the inner array (first element)
            val jsonArray = outerArray.getJSONArray(0)
            if (jsonArray.length() == 0) return "Unknown Mood"

            // Find the highest score
            var maxScoreIndex = 0
            var maxScore = jsonArray.getJSONObject(0).getDouble("score")

            for (i in 1 until jsonArray.length()) {
                val currentScore = jsonArray.getJSONObject(i).getDouble("score")
                if (currentScore > maxScore) {
                    maxScore = currentScore
                    maxScoreIndex = i
                }
            }

            // Get the label with the highest score
            val sentimentLabel = jsonArray.getJSONObject(maxScoreIndex).getString("label").lowercase()

            // Map API label to meaningful sentiment
            val sentiment = when (sentimentLabel) {
                "label_0" -> "negative"  // High Stress, Low Happiness
                "label_1" -> "neutral"   // Medium Mood
                "label_2" -> "positive"  // High Happiness, No Stress
                else -> "mixed"
            }

            val moodDescription = when (sentiment) {
                "negative" -> "It looks like you're feeling stressed and a bit low right now"
                "neutral" -> "You're feeling pretty neutral at the moment"
                "positive" -> "It seems like you're feeling great today"
                else -> "You seem to have mixed emotions, experiencing both positive and negative feelings"
            }

            val recommendation = getRecommendation(sentiment)

            return "$moodDescription. \n $recommendation"

        } catch (e: JSONException) {
            Log.e("SentimentParsing", "JSON Parsing Error: ${e.message}")
            return "Error processing sentiment analysis"
        }
    }

    private fun getRecommendation(sentiment: String): String {
        return when (sentiment) {
            "negative" -> {
                val recommendations = listOf(
                    "Talk to someone you trust—sharing can lighten the load.",
                    "Focus on what you can control and let go of what you can’t.",
                    "Remind yourself that this feeling is temporary—it will pass.",
                    "Hug yourself or hold something comforting, like a pillow or blanket.",
                    "Say something kind to yourself, like “I am doing my best.”"
                )
                "${recommendations.random()}"
            }
            "neutral" -> {
                val recommendations = listOf(
                    "Smile at yourself in the mirror—it can subtly lift your mood.",
                    "Not every day has to be exciting—sometimes, just being is enough.",
                    "A neutral day can be a good time to reflect or just enjoy the stillness.",
                    "Maybe today is a good time to try something simple, like a new song or a short walk.",
                    "You don’t have to do anything big—sometimes, just being present is enough."
                )
                "${recommendations.random()}"
            }
            "positive" -> {
                val recommendations = listOf(
                    "Take a moment to fully enjoy and appreciate this feeling—savor it!",
                    "Write down what’s making you feel good today to look back on later.",
                    "Take a mindful moment and appreciate how good it feels to feel good.",
                    "Use this motivation to learn something new or try a fun challenge.",
                    "Smile and take a deep breath, letting the positivity sink in."
                )
                "${recommendations.random()}"
            }
            else -> "No recommendation available."
        }
    }



}