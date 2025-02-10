package com.example.mentalhealthemotion.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE username = :username")
    fun getUserByUsername(username: String): LiveData<User>

    @Query("SELECT * FROM user WHERE role = :role LIMIT 1")
    suspend fun findUserByRole(role: UserRole): User?

    @Query("SELECT * FROM user")
    fun getAllUsers(): LiveData<List<User>>

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM user WHERE userID = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("SELECT COUNT(*) FROM user WHERE email = :email")
    suspend fun isEmailExists(email: String): Int

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE userID = :userID")
    suspend fun getUserById(userID: Int): User?
}