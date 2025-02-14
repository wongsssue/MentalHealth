package com.example.mentalhealthemotion.Data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "eduContent")
data class EduContent(
    @PrimaryKey val contentID: Int = 0,
    val contentTitle: String = "",
    val contentDescription : String = "",
    val resourceUrl: String = "",
    val imageUrl: String? = null,
    val dateCreated : String? = null
)


