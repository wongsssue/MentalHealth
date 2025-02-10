package com.example.mentalhealthemotion.Data

import androidx.room.TypeConverter

class MoodTypeConverter {
    @TypeConverter
    fun fromMoodType(moodType: MoodType): String {
        return moodType.name
    }

    @TypeConverter
    fun toMoodType(value: String): MoodType {
        return MoodType.valueOf(value)
    }
}