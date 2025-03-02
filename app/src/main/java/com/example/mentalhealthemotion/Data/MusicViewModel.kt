package com.example.mentalhealthemotion.Data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


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

    fun togglePlay(selectedSong: Song) {
        _songs.value = _songs.value.map { song ->
            song.copy(isPlaying = song.title == selectedSong.title && !song.isPlaying)
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

    //Capture user's heart beat through camera method
    private val _heartRate = MutableLiveData(0)
    val heartRate: LiveData<Int> = _heartRate

    private val _heartRateAfter = MutableLiveData(0)
    val heartRateAfter: LiveData<Int> = _heartRateAfter

    private val _calmEffect = MutableStateFlow<Boolean?>(null)
    val calmEffect = _calmEffect.asStateFlow()

    private fun comparePulseReadings() {
        val before = _heartRate.value
        val after = _heartRateAfter.value
        if (before != null && after != null) {
            _calmEffect.value = after < before
        }
    }

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isMeasuring = false
    private val lastNFrames = mutableListOf<Double>() // Stores recent intensities
    private val beatTimestamps = mutableListOf<Long>()  // Store detected heartbeat times
    private var hasCapturedHeartRate = false
    private var cameraProvider: ProcessCameraProvider? = null // Store camera provider

    fun startMeasurement(context: Context, lifecycleOwner: LifecycleOwner) {
        if (isMeasuring) return
        isMeasuring = true
        hasCapturedHeartRate = false

        // CLEAR OLD DATA
        beatTimestamps.clear()
        lastNFrames.clear()
        _heartRate.postValue(0)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get() // Store reference
            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(Surface.ROTATION_0)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                        imageProxy.close()

                        // Stop measurement once BPM is captured within range
                        val currentBPM = _heartRate.value ?: 0
                        if (currentBPM in 30..150 && !hasCapturedHeartRate) {
                            hasCapturedHeartRate = true
                            stopMeasurement(keepLastHeartRate = true)
                        }
                    }
                }

            try {
                cameraProvider?.unbindAll() // Unbind any previous bindings
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("HeartRateMonitor", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Stop Measurement (Reset Everything)
    fun stopMeasurement(keepLastHeartRate: Boolean = false) {
        isMeasuring = false

        if (!keepLastHeartRate) {
            _heartRate.postValue(0) // Reset BPM only if flag is false
        }

        // Ensure camera unbinding runs on the main thread
        Handler(Looper.getMainLooper()).post {
            cameraProvider?.unbindAll()
            cameraProvider = null
        }

        beatTimestamps.clear()
        lastNFrames.clear()
    }

    fun resetMeasurement() {
        hasCapturedHeartRate = false  // Allow rechecking
        _heartRate.postValue(0)       // Reset UI display
    }

    // Process camera frame
    private fun processImage(image: ImageProxy) {
        val planes = image.planes
        if (planes.isNotEmpty()) {
            val buffer = planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val averageIntensity = data.map { it.toInt() and 0xFF }.average()
            val beats = detectBeats(averageIntensity)

            if (beats > 0) {
                _heartRate.postValue(beats)
            }
        }
    }

    // Beat Detection Logic
    private fun detectBeats(intensity: Double): Int {
        val currentTime = System.currentTimeMillis()

        lastNFrames.add(intensity)
        if (lastNFrames.size > 20) lastNFrames.removeAt(0)

        if (lastNFrames.size >= 5) {
            val diff = lastNFrames.last() - lastNFrames.first()
            if (diff > 10 && (beatTimestamps.isEmpty() || currentTime - beatTimestamps.last() > 600)) {
                beatTimestamps.add(currentTime)
                return calculateBPM()
            }
        }
        return _heartRate.value ?: 0
    }

    // Calculate BPM (Use Average of Last 5 Beats)
    private fun calculateBPM(): Int {
        if (beatTimestamps.size < 2) return _heartRate.value ?: 0

        val timeDiffs = beatTimestamps.zipWithNext { a, b -> (b - a) / 1000.0 }
        val avgTimeDiff = timeDiffs.takeLast(5).averageOrNull() ?: return _heartRate.value ?: 0

        return if (avgTimeDiff > 0) (60 / avgTimeDiff).toInt() else _heartRate.value ?: 0
    }

    // Extension Function to Prevent Crashes
    private fun List<Double>.averageOrNull(): Double? = if (isNotEmpty()) average() else null

}