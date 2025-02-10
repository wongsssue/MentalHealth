package com.example.mentalhealthemotion.Data

data class MoodActivityCount(
    val moodType: MoodType,
    val activity: String,
    val count: Int
)


data class ActivityPercentage(
    val activity: String,
    val percentage: Double
)