package com.example.mentalhealthemotion.Data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MusicRepository (
    private val moodEntryDao: MoodEntryDao,
    private val context: Context
){
    private val firestore = FirebaseFirestore.getInstance()
    private val moodEntriesCollection = firestore.collection("moodEntries")

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
             MoodType.rad -> "Mood Booster-Playlist by Spotify"
            MoodType.good -> "Feel good songs playlist"
            MoodType.meh -> "Chill music playlist"
            MoodType.bad -> "Playlist sad"
            MoodType.awful -> "Sad emotional song playlist"
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
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val latestMoodEntry = snapshot.documents.firstOrNull()?.toObject(MoodEntry::class.java)
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

}
