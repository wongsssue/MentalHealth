package com.example.mentalhealthemotion.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY time ASC")
    fun getAllRemindersSortedAscending(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY time DESC")
    fun getAllRemindersSortedDescending(): Flow<List<Reminder>>

    @Insert
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
