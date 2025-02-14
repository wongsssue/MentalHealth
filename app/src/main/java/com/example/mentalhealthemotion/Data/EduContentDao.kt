package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EduContentDao {
    @Insert
    suspend fun insertContent(eduContent: EduContent)

    @Update
    suspend fun updateContent(eduContent: EduContent)

    @Query("DELETE FROM eduContent WHERE contentID = :contentID")
    suspend fun deleteContent(contentID: Int)

    @Query("SELECT * FROM eduContent")
    fun getAllContent(): LiveData<List<EduContent>>

    @Query("SELECT * FROM eduContent WHERE contentID = :contentID")
    suspend fun getContentById(contentID: Int): EduContent?
}