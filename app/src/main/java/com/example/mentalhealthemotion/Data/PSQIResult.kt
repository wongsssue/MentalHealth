package com.example.mentalhealthemotion.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "psqiResults",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userID"],
        childColumns = ["userID"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PSQIResult(
    @PrimaryKey val resultID: Int = 0,
    @ColumnInfo(index = true) val userID: Int,  // Foreign key reference
    val score: Int = 0,
    val date: String? = null,
    val feedback: String = ""
){
    // Empty constructor
    constructor() : this(
        resultID = 0,
        userID = 0,
        score = 0,
        date = null,
        feedback = ""
    )
}

