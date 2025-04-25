package com.example.mentalhealthemotion.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Insert
    suspend fun insertCommunity(community: Community)

    @Update
    suspend fun updateCommunity(community: Community)

    @Delete
    suspend fun deleteCommunity(community: Community)

    @Query("SELECT * FROM community WHERE creatorId = :userId ORDER BY name ASC")
    fun getCommunitiesByUser(userId: Int): Flow<List<Community>>

    @Query("SELECT * FROM community WHERE id = :communityId")
    suspend fun getCommunityById(communityId: String): Community?

    @Query("DELETE FROM community WHERE creatorId = :userId")
    suspend fun deleteAllCommunitiesByUser(userId: Int)

    @Query("SELECT * FROM community ORDER BY name ASC")
    fun getAllCommunities(): Flow<List<Community>>

    @Query("""
        UPDATE community 
        SET members = :members 
        WHERE id = :communityId
    """)
    suspend fun updateCommunityMembers(communityId: String, members: List<Int>)
}
