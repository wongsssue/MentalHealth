package com.example.mentalhealthemotion.Data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import android.util.Log

class ChatRepository(private val db: FirebaseFirestore) {

    // Fetch joined communities for a user
    suspend fun getJoinedCommunities(userName: String): List<Community> {
        return try {
            // Fetch communities from Firestore
            val snapshot = db.collection("Users")
                .document(userName)
                .collection("Communities")
                .get()
                .await()  // Wait for the data

            // Map Firestore documents to Community objects
            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Community::class.java)
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error parsing community: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to fetch joined communities: ${e.message}")
            emptyList()  // Return empty list if an error occurs
        }
    }

    // Fetch messages for a community
    fun getMessages(communityId: String, onMessagesReceived: (List<Message>) -> Unit) {
        db.collection("communities")
            .document(communityId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Error listening for messages: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Map Firestore documents to Message objects
                    val messages = snapshot.documents.mapNotNull { document ->
                        try {
                            document.toObject(Message::class.java)
                        } catch (e: Exception) {
                            Log.e("ChatRepository", "Error parsing message: ${e.message}")
                            null
                        }
                    }
                    onMessagesReceived(messages)
                }
            }
    }

    // Fetch user's name using their ID
    suspend fun getUserName(userId: String): String {
        return try {
            // Fetch user document from Firestore
            val snapshot = db.collection("Users")
                .document(userId)
                .get()
                .await()

            // Get the 'userName' field or return "Unknown" if not found
            snapshot.getString("userName") ?: "Unknown"
        } catch (e: Exception) {
            Log.e("ChatRepository", "Failed to fetch user name: ${e.message}")
            "Unknown"  // Return "Unknown" if an error occurs
        }
    }
}