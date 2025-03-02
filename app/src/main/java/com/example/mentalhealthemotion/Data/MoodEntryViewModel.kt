package com.example.mentalhealthemotion.Data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mentalhealthemotion.R
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

class MoodEntryViewModel(private val repository: MoodEntryRepository) : ViewModel() {
    private val _moodEntries = MutableLiveData<List<MoodEntry>>()
    val moodEntries: LiveData<List<MoodEntry>> get() = _moodEntries

    private var _selectedMood = mutableStateOf(MoodType.meh)
    private var _selectedActivities = mutableStateOf<List<String>>(emptyList())
    private var _quickNote = mutableStateOf("")
    private var _selectedDate = mutableStateOf(generateCurrentDate())
    private var _showPicDialog = mutableStateOf(false)

    var selectedMood: State<MoodType> = _selectedMood
    var selectedActivities: State<List<String>> = _selectedActivities
    var quickNote: State<String> = _quickNote
    var selectedDate: State<String> = _selectedDate
    var showPicDialog:State<Boolean> = _showPicDialog

    private val _audioAttachment = mutableStateOf<String?>(null)
    val audioAttachment: State<String?> = _audioAttachment

    private var _audioDetails = mutableStateOf(Pair("None", "None"))
    var audioDetails: State<Pair<String, String>> = _audioDetails


    fun setAudioDetails(date: String, duration: String) {
        _audioDetails.value = Pair(date, duration)
    }

    fun updateAudio(newAudio: String) {
        _audioAttachment.value = newAudio
    }

    fun updateShowDialog(newValue: Boolean){
        _showPicDialog.value = newValue
    }


    fun updateMood(newMood: MoodType) {
        _selectedMood.value = newMood
    }

    fun toggleActivity(activity: String) {
        _selectedActivities.value = if (_selectedActivities.value.contains(activity)) {
            _selectedActivities.value - activity
        } else {
            _selectedActivities.value + activity
        }
    }

    fun setSelectedActivities(activities: List<String>) {
        _selectedActivities.value = activities
    }

    fun updateQuickNote(note: String) {
        _quickNote.value = note
    }

    fun updateDate(newDate: String) {
        _selectedDate.value = newDate
    }

    fun clearFields() {
        _selectedMood.value = MoodType.meh
        _selectedActivities.value = emptyList()
        _quickNote.value = ""
        _selectedDate.value = generateCurrentDate()
        _audioAttachment.value = ""
        _audioDetails.value = Pair("None","None")
        _imageUri.value = null
        _sentimentResult.value = ""
    }

    private val _currentMoodEntry = MutableLiveData<MoodEntry?>()
    val currentMoodEntry: LiveData<MoodEntry?> = _currentMoodEntry

    fun setCurrentMoodEntry(moodEntry: MoodEntry?) {
        _currentMoodEntry.value = moodEntry
    }

    fun loadMoodEntries(userId: Int) {
        viewModelScope.launch {
            val entries = repository.loadMoodEntries(userId)
            val sortedEntries = entries.sortedByDescending { entry ->
                dateFormat.parse(entry.date)?.time ?: 0L
            }
            _moodEntries.postValue(sortedEntries)
        }
    }

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    fun generateUniqueFourDigitId(): Int {
        val uuid = UUID.randomUUID().toString()
        val hash = uuid.hashCode()
        val positiveHash = Math.abs(hash)
        return (positiveHash % 9000) + 1000
    }

    fun generateCurrentDate(): String {
        val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = timeZone
        return dateFormat.format(Date())
    }


    fun toMoodType(mood: String): MoodType {
        return try {
            MoodType.valueOf(mood.lowercase())  // Convert string to MoodType
        } catch (e: IllegalArgumentException) {
            MoodType.meh  // Return default MoodType if invalid
        }
    }


    fun addMoodEntry(context:Context, userId: Int, onSuccess: () -> Unit) {
        Log.d("MoodEntry", "Adding new mood entry...")
        val moodType = MoodType.valueOf(selectedMood.value.name)
        viewModelScope.launch {
            try {
                val moodEntryId = generateUniqueFourDigitId()
                val currentDate = generateCurrentDate()
                val moodEntry = MoodEntry(
                    moodEntryID = moodEntryId,
                    userID = userId,  // Foreign key reference
                    moodType = moodType,
                    date = currentDate,
                    note = quickNote.value,
                    audioAttachment = filePath?: getDefaultAudioPath(context),
                    activityName = selectedActivities.value,
                    sentimentResult = sentimentResult.value

                )
                repository.insertMoodEntry(userId, moodEntry)
                onSuccess()
                clearFields()
            } catch (e: Exception) {
                "Failed to log entry: ${e.message}"
            }

        }
    }
/*
   fun updateMoodEntry(userId: Int, moodEntry: MoodEntry, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateMoodEntry(userId, moodEntry)
                loadMoodEntries(userId)
                onSuccess()
                clearFields()
                setCurrentMoodEntry(null)
            } catch (e: Exception) {
                "Failed to update entry : ${e.message}"
            }

        }
    }*/

    fun updateMoodEntry(userId: Int, moodEntry: MoodEntry, newFilePath: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val updatedEntry = if (newFilePath != null) {
                    moodEntry.copy(audioAttachment = newFilePath)
                } else {
                    moodEntry
                }
                repository.updateMoodEntry(userId, updatedEntry)
                loadMoodEntries(userId)
                onSuccess()
                clearFields()
                setCurrentMoodEntry(null)
            } catch (e: Exception) {
                Log.e("UpdateMoodEntry", "Failed to update entry: ${e.message}", e)
            }
        }
    }


    fun deleteMoodEntry(userId: Int, moodEntry: MoodEntry) {
        viewModelScope.launch {
            repository.deleteMoodEntry(userId, moodEntry)
            loadMoodEntries(userId)
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var filePath:String? = null
    private var isRecording = false

    fun getAudioFilePath(context: Context): String {
        val timestamp = System.currentTimeMillis() // Unique timestamp
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio_$timestamp.3gp")
        return file.absolutePath
    }

    // Start recording audio
    fun startRecording(context: Context) {
        try {
            if (isRecording) return // Prevent multiple starts

            filePath = getAudioFilePath(context)
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(filePath)
                prepare()
                start()
            }
            isRecording = true
            Log.d("AudioRecorderViewModel", "Recording started")
        } catch (e: Exception) {
            Log.e("AudioRecorderViewModel", "Error starting recording: ${e.message}")
        }
    }

    // Stop recording audio
    fun stopRecording(context: Context): String {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("AudioRecorderViewModel", "Error stopping recording: ${e.message}")
            }
        }
        mediaRecorder = null
        isRecording = false

        val testfilePath = filePath // Get the recorded file path
        Log.d("AudioRecorderViewModel", "Recording stopped. File saved at: $testfilePath")

        val file = File(testfilePath)

        // Retry mechanism to check for file creation
        var attempts = 0
        while (!file.exists() && attempts < 5) {
            Log.w("AudioRecorderViewModel", "File not found, retrying...")
            attempts++
            Thread.sleep(200) // Wait for 200ms before retrying
        }
        if (!file.exists()) {
            Log.e("AudioRecorderViewModel", "File still not found after $attempts retries.")
            return ""
        }
        return testfilePath?: ""
    }


    //default audio file if no audio recorded
    fun getDefaultAudioPath(context: Context): String {
        val outputFile = File(context.getExternalFilesDir("Music"), "default_audio.3gp")

        if (!outputFile.exists()) {
            try {
                val inputStream: InputStream = context.resources.openRawResource(R.raw.dedault_audio)
                val outputStream: OutputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                inputStream.close()
                outputStream.close()
                Log.d("AudioPlayer", "Default audio copied to: ${outputFile.absolutePath}")
            } catch (e: IOException) {
                Log.e("AudioPlayer", "Failed to copy default audio: ${e.message}")
            }
        }

        return outputFile.absolutePath
    }

    //play audio
    fun playAudio(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("AudioPlayer", "File does not exist at path: $filePath")
            return
        }
        stopAudio() // Ensure no duplicate playback
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopAudio()
                    Log.d("AudioPlayer", "Audio playback completed")
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error playing audio: ${e.message}")
            }
        }
        Log.d("AudioPlayer", "Audio playback started")
    }

    // Stop audio playback
    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
        Log.d("AudioPlayer", "Audio playback stopped")
    }


    fun getAudioFileDetails(context: Context, audioUri: String): Pair<String, String> {
        val file = File(audioUri)
        // Extract the duration using MediaPlayer
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.prepare()
        val durationMillis = mediaPlayer.duration
        mediaPlayer.release()
        // Format duration
        val durationFormatted = formatDuration(durationMillis)
        // Extract recording date
        val lastModified = file.lastModified()
        val recordingDate = formatAudioRecordingDate(lastModified)
        return Pair(recordingDate, durationFormatted)
    }

    fun formatDuration(durationMillis: Int): String {
        val minutes = durationMillis / 1000 / 60
        val seconds = (durationMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatAudioRecordingDate(timestamp: Long): String {
        val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        println("Using Time Zone: ${timeZone.id}")
        val dateFormat = SimpleDateFormat("d/M/yyyy, h:mm a", Locale.getDefault())
        dateFormat.timeZone = timeZone
        return dateFormat.format(Date(timestamp))
    }

    //Statistics
    private val _moodCounts = MutableLiveData<List<MoodCount>>()
    val moodCounts: LiveData<List<MoodCount>> get() = _moodCounts


    private val _moodActivityChartData = MutableStateFlow<Map<MoodType, List<BarEntry>>>(emptyMap())
    val moodActivityChartData: StateFlow<Map<MoodType, List<BarEntry>>> = _moodActivityChartData

    private val _moodActivityLabels = MutableStateFlow<Map<MoodType, List<String>>>(emptyMap())
    val moodActivityLabels: StateFlow<Map<MoodType, List<String>>> = _moodActivityLabels

    private val _weeklyMoodChartData = MutableStateFlow<Map<MoodType, List<BarEntry>>>(emptyMap())
    val weeklyMoodChartData: StateFlow<Map<MoodType, List<BarEntry>>> = _weeklyMoodChartData

    private val _weeklyMoodLabels = MutableStateFlow<List<String>>(emptyList())
    val weeklyMoodLabels: StateFlow<List<String>> = _weeklyMoodLabels



    fun countMoodsForCurrentMonth(userId: Int) {
        viewModelScope.launch {
            val moodCountsList = repository.countMoodsForCurrentMonth(userId)
            _moodCounts.postValue(moodCountsList)
        }
    }

   fun createMoodActivityDataForMonth(userId: Int) {
        viewModelScope.launch {
            repository.getMoodActivityDataForMonth(userId).collect { moodDataMap ->
                val moodEntriesMap = moodDataMap.mapValues { (_, activityPercentages) ->
                    activityPercentages.mapIndexed { index, item ->
                        BarEntry(index.toFloat(), item.percentage.toFloat()) // X = index, Y = percentage
                    }
                }

                val moodLabelsMap = moodDataMap.mapValues { (_, activityPercentages) ->
                    activityPercentages.map { it.activity }
                }

                _moodActivityChartData.value = moodEntriesMap
                _moodActivityLabels.value = moodLabelsMap
            }
        }
    }

    fun createMoodDataForCurrentWeek(userId: Int) {
        viewModelScope.launch {
            repository.getMoodsForCurrentWeek(userId).collect { moodDataMap ->
                val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

                val moodEntriesMap = MoodType.values().associateWith { moodType ->
                    weekDays.mapIndexed { index, day ->
                        val moodCountsForMood = moodDataMap[moodType] ?: emptyList()

                        val countForDay = moodCountsForMood.find { convertDayToWeekday(it.day) == day }?.mood_count ?: 0
                        Log.d("MoodMapping", "MoodType: $moodType, Day: $day, Count: $countForDay")

                        BarEntry(index.toFloat(), countForDay.toFloat()) // X = weekday index, Y = count
                    }
                }
                _weeklyMoodChartData.value = moodEntriesMap
                _weeklyMoodLabels.value = weekDays
            }
        }
    }

    fun convertDayToWeekday(day: String): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        try {
            val dayInt = day.toInt()
            calendar.set(currentYear, currentMonth, dayInt)
            // Convert to weekday name
            val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            return dateFormat.format(calendar.time)
        } catch (e: Exception) {
            Log.e("DateConversion", "Invalid day: $day", e)
            return "Unknown"
        }
    }

    //Face emotion detect
    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun updateImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    fun prepareCamera(context: Context) {
        val timeStamp = System.currentTimeMillis() 
        photoFile = File(context.cacheDir, "captured_image_$timeStamp.jpg")
        photoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    fun getPhotoUri(): Uri {
        return photoUri ?: Uri.EMPTY // Avoid returning null
    }

    fun onPhotoCaptured(success: Boolean, context: Context, onNavigate: (String) -> Unit) {
        if (success) {
            updateImageUri(photoUri) // Notify UI
            detectEmotion(context, photoUri, onNavigate) // Auto-send to API
        }
    }

    fun detectEmotion(context: Context, imageUri: Uri, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            val mood = repository.detectEmotion(context, imageUri)
            _selectedMood.value = mood
            onNavigate("EditEntryPage?isEditing=false")
           _showPicDialog.value = false
        }
    }


    //sentiment analysis (note)
    private var _sentimentResult = mutableStateOf("")
    var sentimentResult: State<String> = _sentimentResult


    fun updateSentimentResult(newResult: String) {
        _sentimentResult.value = newResult
    }


    fun analyzeNote() {
        viewModelScope.launch {
            val note = _quickNote.value.trim()
            if (note.isBlank()) {
                _sentimentResult.value = "No note found"
                return@launch
            }

            repository.analyzeSentiment(note) { result ->
                viewModelScope.launch {
                    updateSentimentResult(result)
                }
            }
        }
    }

}
