package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    @Insert
    suspend fun insertMoodEntry(moodEntry: MoodEntry)

    @Update
    suspend fun updateMoodEntry(moodEntry: MoodEntry)

    @Delete
    suspend fun deleteMoodEntry(moodEntry: MoodEntry)

    @Query("SELECT * FROM moodEntry WHERE userID = :userId ORDER BY date DESC")
    fun getAllMoodEntriesByUser(userId: Int): List<MoodEntry>

    @Query("SELECT * FROM moodEntry WHERE moodEntryID = :moodEntryId")
    suspend fun getMoodEntryById(moodEntryId: Int): MoodEntry?

    @Query("DELETE FROM moodEntry WHERE userID = :userId")
    suspend fun deleteAllMoodEntriesByUser(userId: Int)

    @Query("""
    SELECT moodType, COUNT(*) as mood_count
    FROM moodEntry 
    WHERE SUBSTR(date, 4, 7) = :monthYear AND userId = :userId
    GROUP BY moodType""")
    suspend fun countMoodsByMonthYear(userId: Int, monthYear: String): List<MoodCount>

    //Retrieve activities for a specific mood in a given month
    @Query("""
    SELECT *
    FROM moodEntry 
    WHERE SUBSTR(date, 4, 7) = :monthYear AND userId = :userId""")
    fun getActivitiesForMoodInMonth(userId: Int, monthYear: String): Flow<List<MoodEntry>>

    @Query("""
    SELECT SUBSTR(date, 1, 2) AS day, moodType, COUNT(*) as mood_count
    FROM moodEntry 
    WHERE date BETWEEN :startDate AND :endDate AND userId = :userId
    GROUP BY day, moodType
    ORDER BY day ASC
""")
    fun countMoodsByWeek(userId: Int, startDate: String, endDate: String): Flow<List<MoodCountByDay>>

    @Query("SELECT * FROM moodEntry WHERE userID = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMoodEntry(userId: Int): MoodEntry?


    @Query("""
    SELECT * 
    FROM moodEntry
    WHERE SUBSTR(date, 4, 7) = :monthYear AND userId = :userId
""")
    fun getMoodEntriesForMonth(userId: Int, monthYear: String): Flow<List<MoodEntry>>

    @Query("SELECT note FROM moodEntry WHERE moodEntryID = :moodEntryId")
     fun getUserNote(moodEntryId: Int): String?
}