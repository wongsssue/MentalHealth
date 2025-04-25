package com.example.mentalhealthemotion.Data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentalhealthemotion.API.YoutubeRetrofitInstance
import com.example.mentalhealthemotion.Data.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel(private val apiKey: String) : ViewModel() {
    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos

    fun fetchVideos(category: String = "motivational videos") {
        viewModelScope.launch {
            try {
                val response = YoutubeRetrofitInstance.api.getVideos(query = category, apiKey = apiKey)
                _videos.value = response.items.map { video ->
                    video.copy(category = category) // Assign selected category
                }
            } catch (e: Exception) {
                Log.e("YouTubeAPI", "Error fetching videos: ${e.message}")
            }
        }
    }
}