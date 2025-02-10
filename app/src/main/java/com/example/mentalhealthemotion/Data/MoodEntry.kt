package com.example.mentalhealthemotion.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "moodEntry",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userID"],
        childColumns = ["userID"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MoodEntry(
    @PrimaryKey val moodEntryID: Int = 0,
    @ColumnInfo(index = true) val userID: Int,  // Foreign key reference
    val moodType: MoodType = MoodType.meh,
    val date: String? = null,
    val note: String = "",
    val audioAttachment: String? = null,
    val activityName: List<String> = emptyList()
) {
    // Empty constructor
    constructor() : this(
        moodEntryID = 0,
        userID = 0,
        moodType = MoodType.meh,
        date = null,
        note = "",
        audioAttachment = null,
        activityName = emptyList()
    )
}
