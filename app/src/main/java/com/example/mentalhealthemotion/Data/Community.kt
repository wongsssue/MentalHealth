package com.example.mentalhealthemotion.Data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.*
import java.util.UUID

@Entity(
    tableName = "community",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userID"],
        childColumns = ["creatorId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Community(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val description: String = "",
    val creatorId: String = "",  // Ensure this matches Firestore data type
    val members: List<String> = emptyList(), // Change List<Int> → List<String>
    val createdAt: Long = 0L
)

