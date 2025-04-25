package com.example.mentalhealthemotion.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val time: Long, // store the reminder time as a timestamp
    val isActive: Boolean = true
)
