package com.example.mentalhealthemotion.Data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CommunityRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val communitiesCollection = firestore.collection("communities")

    suspend fun insertCommunity(community: Community) = withContext(Dispatchers.IO) {
        try {
            communitiesCollection.document(community.id).set(community).await()
            Log.d("CommunityRepository", "Community saved in Firestore.")
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Failed to save community: ${e.message}", e)
            throw e
        }
    }

    suspend fun getAllCommunities(): List<Community> = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = communitiesCollection.get().await()
            Log.d("CommunityRepository", "Documents found: ${snapshot.documents.size}")

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                // Convert members field manually
                val members = (data["members"] as? List<Any>)?.map { it.toString() } ?: emptyList()

                Community(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    creatorId = data["creatorId"] as? String ?: "",
                    members = members, // ✅ Ensuring conversion
                    createdAt = data["createdAt"] as? Long ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Failed to retrieve communities: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addUserToCommunity(userId: Int, communityId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.runTransaction { transaction ->
                val userRef = communitiesCollection.document(communityId).collection("members").document(userId.toString())
                transaction.set(userRef, mapOf("userId" to userId))
            }.await()
            Log.d("CommunityRepository", "User $userId added to community $communityId in Firestore.")
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Failed to add user to community: ${e.message}", e)
            throw e
        }
    }

    suspend fun getJoinedCommunities(userId: String): List<Community> {
        Log.d("CommunityRepository", "📢 Fetching communities where members contain: $userId")

        val querySnapshot = Firebase.firestore.collection("communities")
            .whereArrayContains("members", userId)
            .get()
            .await()

        val communities = querySnapshot.toObjects(Community::class.java)

        Log.d("CommunityRepository", "✅ Fetched communities: ${communities.size}")
        Log.d("CommunityRepository", "✅ Community IDs: ${communities.map { it.id }}")

        return communities
    }

    suspend fun updateCommunity(community: Community) {
        val communityRef = FirebaseFirestore.getInstance().collection("communities").document(community.id)
        communityRef.set(community)
            .addOnSuccessListener { Log.d("Firebase", "Community updated successfully") }
            .addOnFailureListener { Log.e("Firebase", "Error updating community", it) }
    }

    suspend fun deleteCommunity(communityId: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection("communities")
                .document(communityId)
                .delete()
                .await()
            Log.d("CommunityRepository", "✅ Successfully deleted community: $communityId")
        } catch (e: Exception) {
            Log.e("CommunityRepository", "❌ Error deleting community", e)
        }
    }




}