package com.example.mentalhealthemotion.Data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MusicViewModel(private val musicRepository: MusicRepository) :ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> get() = _songs

    fun loadSongsForMood(mood: MoodType) {
        viewModelScope.launch(Dispatchers.IO) {
            val songsList = musicRepository.fetchSongsForMood(mood)
            withContext(Dispatchers.Main) {
                _songs.value = songsList
            }
        }
    }


    fun sortSongs(isSortedAlphabetically: Boolean) {
        _songs.value = if (isSortedAlphabetically) {
            _songs.value.sortedBy { it.title }
        } else {
            _songs.value.shuffled()
        }
    }

    fun togglePlay(context: Context, selectedSong: Song) {
        _songs.value = _songs.value.map { song ->
            if (song.title == selectedSong.title) {
                if (!song.isPlaying) {
                    // Open YouTube when the song is played
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${song.videoId}"))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
                song.copy(isPlaying = !song.isPlaying)
            } else {
                song.copy(isPlaying = false) // Stop other songs
            }
        }
    }

    fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private val _moodEntry = MutableLiveData<MoodEntry?>()
    val moodEntry: LiveData<MoodEntry?> get() = _moodEntry

    fun getLatestMoodEntry(userId: Int) {
        viewModelScope.launch {
            try {
                val latestMood = musicRepository.getLatestMoodEntry(userId)
                latestMood?.let {
                    Log.d("Latest Mood", "Mood: ${it.moodType}, Date: ${it.date}")
                } ?: Log.d("Latest Mood", "No mood entry found")
                _moodEntry.postValue(latestMood)
            } catch (e: Exception) {
                Log.e("Latest Mood", "Error fetching latest mood", e)
            }
        }
    }

}