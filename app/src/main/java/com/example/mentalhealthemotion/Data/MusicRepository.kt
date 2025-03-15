package com.example.mentalhealthemotion.Data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.net.URLEncoder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Locale

class MusicRepository (
    private val moodEntryDao: MoodEntryDao,
    private val context: Context
){
    private val firestore = FirebaseFirestore.getInstance()
    private val moodEntriesCollection = firestore.collection("moodEntries")
    private lateinit var tflite: Interpreter

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val assetFileDescriptor = context.assets.openFd("mood_model.tflite")
            val fileInputStream = assetFileDescriptor.createInputStream()
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val length = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
            tflite = Interpreter(modelBuffer)
        } catch (e: Exception) {
            Log.e("MoodClassifier", "Error loading TFLite model: ${e.message}")
        }
    }
/*
    fun predictSongs(mood: MoodType): List<Song> {
        val moodMap = mapOf(
            MoodType.rad to 4,
            MoodType.meh to 3,
            MoodType.good to 2,
            MoodType.bad to 1,
            MoodType.awful to 0
        )

        val moodIndex = moodMap[mood] ?: return emptyList()
       // val inputArray = arrayOf(floatArrayOf(moodIndex.toFloat())) // Shape [1,1]
        val inputArray = arrayOf(
            floatArrayOf(
                moodIndex.toFloat(),  // Convert Int to Float
                1.0f,                 // Convert 1 to Float
                120.0f,               // Convert 120 to Float
                1.0f,                 // Convert 1 to Float
                1.0f,                 // Convert 1 to Float
                1.0f                  // Convert 1 to Float
            )
        )


        Log.d("MusicRepository", "Input to model: ${inputArray.contentDeepToString()}")


        // Ensure output shape matches the model's output
        val outputArray = Array(1) { FloatArray(5) } // Adjust this based on actual model output

        Log.d("MusicRepository", "Raw Model Output: ${outputArray.contentDeepToString()}")
        try {
            if (tflite == null) {
                Log.d("MusicRepository", "TFLite model is not loaded!")
                return emptyList()
            }

            tflite?.run(inputArray, outputArray)

            if (outputArray.isEmpty() || outputArray[0].isEmpty()) {
                Log.d("MusicRepository", "Model returned empty output!")
                return emptyList()
            }

            val predictedGenreIndex = outputArray[0].indices.maxByOrNull { outputArray[0][it] } ?: return emptyList()

            val genreLookUp = mapOf(
                0 to "Pop", 1 to "Classical", 2 to "Rock",
                3 to "Hip-Hop", 4 to "Funk", 5 to "Ambient"
            )

            val moodLookUp = mapOf(
                0 to "Melancholic", 1 to "Joyful", 2 to "Soothing",
                3 to "Energetic", 4 to "Emotional", 5 to "Powerful", 6 to "Calm"
            )

            return fetchSongsByGenreMood( moodLookUp[predictedGenreIndex] ?: "")
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error running inference: ${e.message}", e)
            return emptyList()
        }
    }*/

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val apiKey = "AIzaSyDeAnkrcA3QlOzs-7JeJEbh8DkBcB2cA6I"
    private val client = OkHttpClient()

    fun fetchSongsForMood(mood: MoodType): List<Song> {
        val query = when (mood) {
            MoodType.rad -> "Super happy playlist"
            MoodType.good -> "Feel good songs playlist"
            MoodType.meh -> "Mood Booster playlist"
            MoodType.bad ->   "Relaxing Lofi Chillhop"
            MoodType.awful -> "Gentle Classical Piano"

        }

        val searchUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=5&q=${query}&key=$apiKey"
        val searchRequest = Request.Builder().url(searchUrl).build()

        return try {
            val searchResponse = client.newCall(searchRequest).execute()
            val searchBody = searchResponse.body?.string()

            if (searchBody.isNullOrEmpty()) {
                Log.e("MusicRepository", "Empty search API response")
                return emptyList()
            }

            val searchJson = JSONObject(searchBody)
            val items = searchJson.optJSONArray("items") ?: return emptyList()

            val videoIds = mutableListOf<String>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                if (item.has("id") && item.getJSONObject("id").has("videoId")) {
                    videoIds.add(item.getJSONObject("id").getString("videoId"))
                }
            }

            if (videoIds.isEmpty()) {
                Log.e("MusicRepository", "No video IDs found in search response.")
                return emptyList()
            }

            val detailsUrl = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,snippet&id=${videoIds.joinToString(",")}&key=$apiKey"
            val detailsRequest = Request.Builder().url(detailsUrl).build()
            val detailsResponse = client.newCall(detailsRequest).execute()
            val detailsBody = detailsResponse.body?.string()

            if (detailsBody.isNullOrEmpty()) {
                Log.e("MusicRepository", "Empty video details API response")
                return emptyList()
            }

            val detailsJson = JSONObject(detailsBody)
            val detailsItems = detailsJson.optJSONArray("items") ?: return emptyList()

            (0 until detailsItems.length()).mapNotNull { i ->
                val item = detailsItems.getJSONObject(i)
                val snippet = item.optJSONObject("snippet") ?: return@mapNotNull null
                val contentDetails = item.optJSONObject("contentDetails") ?: return@mapNotNull null
                val videoId = item.optString("id", "")
                if (videoId.isEmpty()) return@mapNotNull null

                val author = snippet.optString("channelTitle", "Unknown")
                val duration = parseDuration(contentDetails.optString("duration", "PT0S"))

                Song(
                    title = snippet.optString("title", "Unknown"),
                    videoId = videoId,
                    authorName = author,
                    durationInSeconds = duration,
                    isPlaying = false
                )
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching songs: ${e.message}", e)
            emptyList()
        }
    }
/*
    private fun fetchSongsByGenreMood(mood: String): List<Song> {
        val encodedQuery = URLEncoder.encode("$mood music", "UTF-8")
        val searchUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=5&q=$encodedQuery&key=$apiKey"

        val searchRequest = Request.Builder().url(searchUrl).build()

        return try {
            val searchResponse = client.newCall(searchRequest).execute()
            val searchBody = searchResponse.body?.string()

            if (searchBody.isNullOrEmpty()) {
                Log.e("MusicRepository", "Empty search API response")
                return emptyList()
            }

            val searchJson = JSONObject(searchBody)
            val items = searchJson.optJSONArray("items") ?: return emptyList()

            val videoIds = mutableListOf<String>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val videoId = item.optJSONObject("id")?.optString("videoId", "")
                if (!videoId.isNullOrEmpty()) videoIds.add(videoId)
            }

            if (videoIds.isEmpty()) {
                Log.e("MusicRepository", "No video IDs found in search response.")
                return emptyList()
            }

            val detailsUrl = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,snippet&id=${videoIds.joinToString(",")}&key=$apiKey"
            val detailsRequest = Request.Builder().url(detailsUrl).build()
            val detailsResponse = client.newCall(detailsRequest).execute()
            val detailsBody = detailsResponse.body?.string()

            if (detailsBody.isNullOrEmpty()) {
                Log.e("MusicRepository", "Empty video details API response")
                return emptyList()
            }

            val detailsJson = JSONObject(detailsBody)
            val detailsItems = detailsJson.optJSONArray("items") ?: return emptyList()

            return (0 until detailsItems.length()).mapNotNull { i ->
                val item = detailsItems.getJSONObject(i)
                val snippet = item.optJSONObject("snippet") ?: return@mapNotNull null
                val contentDetails = item.optJSONObject("contentDetails") ?: return@mapNotNull null
                val videoId = item.optString("id", "")

                if (videoId.isEmpty()) return@mapNotNull null

                Song(
                    title = snippet.optString("title", "Unknown"),
                    videoId = videoId,
                    authorName = snippet.optString("channelTitle", "Unknown"),
                    durationInSeconds = parseDuration(contentDetails.optString("duration", "PT0S")),
                    isPlaying = false
                )
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching songs: ${e.message}", e)
            emptyList()
        }
    }
*/
    fun parseDuration(duration: String): Int {
        val pattern = "PT(\\d+H)?(\\d+M)?(\\d+S)?".toRegex()
        val matchResult = pattern.matchEntire(duration)

        if (matchResult != null) {
            val hours = matchResult.groupValues[1].removeSuffix("H").toIntOrNull() ?: 0
            val minutes = matchResult.groupValues[2].removeSuffix("M").toIntOrNull() ?: 0
            val seconds = matchResult.groupValues[3].removeSuffix("S").toIntOrNull() ?: 0
            return (hours * 3600) + (minutes * 60) + seconds
        }

        return 0
    }

    suspend fun getLatestMoodEntry(userId: Int): MoodEntry? {
        return try {
            if (isOnline()) {
                val snapshot = moodEntriesCollection
                    .document(userId.toString())
                    .collection("moodEntries")
                    .get()
                    .await()

                val sortedEntries = snapshot.documents
                    .mapNotNull { it.toObject(MoodEntry::class.java) }
                    .sortedByDescending { entry -> dateFormat.parse(entry.date)?.time ?: 0L }  // Manual sorting

                val latestMoodEntry = sortedEntries.firstOrNull()  // Now, this is truly the latest

                Log.d("MoodEntryRepository", "Latest mood entry fetched from Firestore.")
                latestMoodEntry
            } else {
                moodEntryDao.getLatestMoodEntry(userId)
            }
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to fetch latest mood entry: ${e.message}")
            moodEntryDao.getLatestMoodEntry(userId)
        }
    }
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

}
