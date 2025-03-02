package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PSQIDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: PSQIResult)

    @Query("SELECT * FROM psqiResults WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllResults(userId: Int): List<PSQIResult>

    @Query("SELECT * FROM psqiResults WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getResultsByDateRange(startDate: String, endDate: String): LiveData<List<PSQIResult>>

    @Delete
    suspend fun deleteResult(result: PSQIResult)

    @Query("""
    SELECT * 
    FROM psqiResults
    WHERE SUBSTR(date, 4, 7) = :monthYear AND userId = :userId
""")
    fun getSleepQualityForMonth(userId: Int, monthYear: String): Flow<List<PSQIResult>>

    @Query("""
    SELECT CAST(SUBSTR(date, 1, 2) AS INT) AS day, 
    AVG(score) AS sleepScore
    FROM psqiResults
    WHERE SUBSTR(date, 4, 7) = :monthYear AND userID = :userId
    ORDER BY day ASC
    """)
    suspend fun getDailySleepFeedback(userId: Int, monthYear: String): List<DailySleepScore>
}
