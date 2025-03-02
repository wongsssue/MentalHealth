package com.example.mentalhealthemotion.Data

import androidx.room.ColumnInfo

data class MoodCount(
    @ColumnInfo(name = "moodType") val moodType: String,
    @ColumnInfo(name = "mood_count") val count: Int
)

data class MoodCountByDay(
    val day: String,         // "01", "02", etc.
    val moodType: MoodType,
    val mood_count: Int
)

data class DailySleepScore(
    val day: Int,       // X-axis (Day of the month)
    val sleepScore: Float // Y-axis (Sleep quality score)
)
