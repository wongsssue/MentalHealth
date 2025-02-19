package com.example.mentalhealthemotion.Data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class PSQIVIewModel(private val repository: PSQIRepository) : ViewModel() {
    private val _questions = MutableLiveData<List<PSQIQuestion>>()
    val questions: LiveData<List<PSQIQuestion>> get() = _questions

    private val _responses = mutableStateListOf<PSQIResponse>()
    val responses: List<PSQIResponse> get() = _responses

    private val _result = MutableLiveData<PSQIResult>()
    val result: LiveData<PSQIResult> = _result

    fun updateResponse(questionId: Int, answer: Any) {
        val index = _responses.indexOfFirst { it.questionId == questionId }
        if (index != -1) {
            _responses[index] = PSQIResponse(questionId, answer)
        } else {
            _responses.add(PSQIResponse(questionId, answer))
        }
    }

    fun clearResponses() {
        _responses.clear()  // Clear all previous responses
    }


    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        _questions.value = repository.getPSQIQuestions()
    }

    fun deleteResult(userId: Int, result: PSQIResult) {
        viewModelScope.launch {
            repository.deleteResult(userId, result)
        }
    }

    private val _pastResults = MutableLiveData<List<PSQIResult>>()
    val pastResults: LiveData<List<PSQIResult>> get() = _pastResults

    fun getAllResults(userId: Int) {
        viewModelScope.launch {
            val results = repository.getAllResults(userId)
            val sortedResults = results.sortedByDescending { result ->
                dateFormat.parse(result.date)?.time ?: 0L
            }
            _pastResults.postValue(sortedResults)
        }
    }

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    fun generateCurrentDate(): String {
        val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = timeZone
        return dateFormat.format(Date())
    }

    fun generateUniqueFourDigitId(): Int {
        val uuid = UUID.randomUUID().toString()
        val hash = uuid.hashCode()
        val positiveHash = Math.abs(hash)
        return (positiveHash % 9000) + 1000
    }

    fun submitResponses(userID: Int, responses: List<PSQIResponse>, onSuccess: () -> Unit,) {
        viewModelScope.launch {
            try{
                val score = calculatePSQIScore(responses)
                val result = PSQIResult(
                    resultID = generateUniqueFourDigitId(),
                    userID= userID,
                    score = score,
                    date = generateCurrentDate(),
                    feedback = feedBack(score)
                )
                repository.insertResult(userID, result)
                _result.postValue(result)
                onSuccess()
            } catch (e: Exception) {
                Log.e("PSQIVIewModel", "Failed to submit result: ${e.message}", e)
            }
        }
    }


    fun feedBack(score: Int): String {
        return when (score) {
            in 0..5 -> "Good sleep quality"
            in 6..10 -> "Moderate sleep disturbances"
            else -> "Poor sleep quality, consider consulting a doctor"
        }
    }

    fun calculatePSQIScore(responses: List<PSQIResponse>): Int {
        var C1 = 0 // Subjective Sleep Quality
        var C2 = 0 // Sleep Latency
        var C3 = 0 // Sleep Duration
        var C4 = 0 // Sleep Efficiency
        var C5 = 0 // Sleep Disturbances
        var C6 = 0 // Use of Sleep Medication
        var C7 = 0 // Daytime Dysfunction
        var C2_5 = 0

        var bedtime: Calendar? = null
        var wakeupTime: Calendar? = null
        var sleepDuration: Double? = null

        responses.forEach { response ->
            when (response.questionId) {
                14 -> { // Subjective Sleep Quality
                    C1 = mapResponseToScore(response.answer, listOf("Very Good", "Fairly Good", "Fairly Bad", "Very Bad"))
                }
                2 -> { // Sleep Latency (Minutes to fall asleep)
                    val minutes = (response.answer as? Int) ?: response.answer.toString().toIntOrNull() ?: 0
                    Log.d("DEBUG", "Question ${response.questionId}: Answer Type = ${response.answer::class.simpleName}")

                    C2 = when {
                        minutes <= 15 -> 0
                        minutes in 16..30 -> 1
                        minutes in 31..60 -> 2
                        else -> 3
                    }
                }
                5 -> {
                    C2_5 = mapResponseToScore(response.answer, listOf("Not during the past month", "Less than once a week", "Once or twice a week", "Three or more times a week"))
                    C2 += C2_5
                    C2 = when {
                        C2 == 0 -> 0
                        C2 in 1..2 -> 1
                        C2 in 3..4 -> 2
                        else -> 3
                    }
                }
                4 -> { // Sleep Duration (Hours slept)
                    val duration = (response.answer as? Double) ?: response.answer.toString().toDoubleOrNull() ?: 0.0
                    Log.d("DEBUG", "Question ${response.questionId}: Answer Type = ${response.answer::class.simpleName}")

                    sleepDuration = duration
                    C3 = when {
                        duration >= 7.0 -> 0
                        duration in 6.0..6.9 -> 1
                        duration in 5.0..5.9 -> 2
                        else -> 3
                    }
                }
                1 -> { // Bedtime (String format like "23:30")
                    bedtime = parseTime(response.answer as? String)
                }
                3 -> { // Wake-up time (String format like "06:30")
                    wakeupTime = parseTime(response.answer as? String)
                }
                6, 7, 8, 9, 10, 11, 12, 13 -> { // Sleep Disturbances
                    C5 += mapResponseToScore(response.answer, listOf("Not during the past month", "Less than once a week", "Once or twice a week", "Three or more times a week"))
                }
                15 -> { // Use of Sleep Medication
                    C6 = mapResponseToScore(response.answer, listOf("Not during the past month", "Less than once a week", "Once or twice a week", "Three or more times a week"))
                }
                16, 17 -> { // Daytime Dysfunction
                    C7 += mapResponseToScore(response.answer, listOf("No problem at all", "Only a very slight problem", "Somewhat of a problem", "A very big problem"))
                }
            }
        }

        // Compute Sleep Efficiency (C4)
        if (bedtime != null && wakeupTime != null && sleepDuration != null) {
            val timeInBed = getHoursBetween(bedtime, wakeupTime)
            val sleepHours = sleepDuration ?: 0.0
            val sleepEfficiency = if (timeInBed > 0) (sleepHours / timeInBed) * 100 else 0.0

            C4 = when {
                sleepEfficiency > 85 -> 0
                sleepEfficiency in 75.0..84.9 -> 1
                sleepEfficiency in 65.0..74.9 -> 2
                else -> 3
            }
        }

        // Normalize C5 & C7 (each contributes only once, so limit to max 3)
        C5 = C5.coerceIn(0, 3)
        C7 = C7.coerceIn(0, 3)

        // Total PSQI Score
        return C1 + C2 + C3 + C4 + C5 + C6 + C7
    }

    fun parseTime(timeString: String?): Calendar? {
        if (timeString.isNullOrBlank()) return null

        val timeRegex = Regex("^(\\d{1,2}):(\\d{2})([aApP][mM])$")
        val matchResult = timeRegex.find(timeString.trim())

        return if (matchResult != null) {
            val (hourStr, minuteStr, amPm) = matchResult.destructured
            var hour = hourStr.toInt()
            val minute = minuteStr.toInt()

            // Convert 12-hour format to 24-hour format
            when (amPm.lowercase()) {
                "am" -> if (hour == 12) hour = 0  // 12 AM -> 00
                "pm" -> if (hour != 12) hour += 12 // PM conversion
            }

            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
        } else {
            Log.e("PSQIVIewModel", "Invalid time format: $timeString")
            null
        }
    }


    // Calculate time in bed (handling bedtime past midnight)
    fun getHoursBetween(start: Calendar?, end: Calendar?): Double {
        if (start == null || end == null) return 0.0
        val startMillis = start.timeInMillis
        var endMillis = end.timeInMillis

        // If wake-up time is before bedtime, assume it's past midnight
        if (endMillis < startMillis) {
            endMillis += 24 * 60 * 60 * 1000 // Add 24 hours
        }

        return ((endMillis - startMillis) / (1000.0 * 60 * 60))
    }

    // Helper function to map response text to scores
    fun mapResponseToScore(answer: Any, options: List<String>): Int {
        return options.indexOf(answer).coerceAtLeast(0)
    }

    private val _inputError = MutableLiveData<String?>("This field cannot be empty")
    val inputError: LiveData<String?> = _inputError


    fun getValidationError(input: String, questionId: Int): Boolean {
        val trimmedInput = input.trim().uppercase()

        return when (questionId) {
            1, 3 -> { // Bedtime & Wake-up Time
                val timePattern = """^(0[1-9]|1[0-2]):[0-5][0-9]\s?(AM|PM)$""".toRegex()
                when {
                    trimmedInput.isBlank() -> {
                        _inputError.value = "Please input a time."
                        false
                    }
                    !timePattern.matches(trimmedInput) -> {
                        _inputError.value = "Please enter a valid time in HH:MM AM/PM format (e.g., 10:30 PM or 06:15 AM)."
                        false
                    }
                    else -> {
                        _inputError.value = null
                        true
                    }
                }
            }

            2 -> { // Time to fall asleep (in minutes)
                val minutes = trimmedInput.toIntOrNull()
                when {
                    trimmedInput.isBlank() -> {
                        _inputError.value = "Time to fall asleep cannot be empty."
                        false
                    }
                    minutes == null -> {
                        _inputError.value = "Please enter a number for minutes (e.g., 15)."
                        false
                    }
                    minutes < 0 -> {
                        _inputError.value = "Invalid value. Please enter a valid value."
                        false
                    }
                    minutes > 800 -> {
                        _inputError.value = "That seems too high! Please enter a valid value."
                        false
                    }
                    else -> {
                        _inputError.value = null
                        true
                    }
                }
            }

            4 -> { // Hours of actual sleep
                val hours = trimmedInput.toFloatOrNull()
                when {
                    trimmedInput.isBlank() -> {
                        _inputError.value = "Sleep hours cannot be empty."
                        false
                    }
                    hours == null -> {
                        _inputError.value = "Please enter a number for sleep hours (e.g., 7 or 8.5)."
                        false
                    }
                    hours < 0 -> {
                        _inputError.value = "Invalid sleep hours. Please enter a valid value."
                        false
                    }
                    hours > 24 -> {
                        _inputError.value = "Invalid sleep hours. Please enter a valid value."
                        false
                    }
                    else -> {
                        _inputError.value = null
                        true
                    }
                }
            }

            else -> { // Generic check for all other questions
                if (trimmedInput.isEmpty()) {
                    _inputError.value = "This field cannot be empty."
                    false
                } else {
                    _inputError.value = null
                    true
                }
            }
        }
    }
}
