package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

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
}
