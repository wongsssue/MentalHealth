package com.example.mentalhealthemotion.Data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.UUID

class CommunityViewModel(private val repository: CommunityRepository) : ViewModel() {

    // LiveData for all communities and joined communities
    private val _allCommunities = MutableLiveData<List<Community>>()
    val allCommunities: LiveData<List<Community>> = _allCommunities

    private val _joinedCommunities = MutableLiveData<List<Community>>()
    val joinedCommunities: LiveData<List<Community>> = _joinedCommunities

    private val _selectedCommunity = MutableLiveData<Community?>()
    val selectedCommunity: LiveData<Community?> = _selectedCommunity

    private val db = FirebaseFirestore.getInstance()

    // Save a new community to the repository and Firestore
    fun saveCommunity(userId: Int, name: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val communityId = UUID.randomUUID().toString()
                val userIdString = userId.toString()

                val community = Community(
                    id = communityId,
                    name = name,
                    description = description,
                    creatorId = userIdString,
                    members = listOf(userIdString) // Add creator as the first member
                )

                repository.insertCommunity(community)  // Save to repository
                fetchJoinedCommunities(userIdString)  // Refresh joined communities
                loadAllCommunities()  // Refresh all communities
                onSuccess()
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Failed to save community: ${e.message}", e)
            }
        }
    }

    // Load all communities from the repository
    fun loadAllCommunities() {
        viewModelScope.launch {
            try {
                val communities = repository.getAllCommunities()
                _allCommunities.postValue(communities)
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error loading all communities: ${e.message}", e)
            }
        }
    }

    // Load communities that a user has joined
    fun loadCommunityEntries(userId: Int) {
        viewModelScope.launch {
            try {
                val communities = repository.getJoinedCommunities(userId.toString())
                val sortedCommunities = communities.sortedByDescending { it.createdAt }
                _joinedCommunities.value = sortedCommunities
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error loading joined communities: ${e.message}", e)
            }
        }
    }

    // Add user to a community (join a community)
    fun joinCommunity(userId: String, communityId: String) {
        val communityRef = db.collection("communities").document(communityId)

        communityRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                communityRef.update("members", FieldValue.arrayUnion(userId)) // Add user to community members
                    .addOnSuccessListener {
                        Log.d("CommunityViewModel", "User joined successfully!")
                        fetchJoinedCommunities(userId)  // Refresh joined communities list
                    }
                    .addOnFailureListener { e ->
                        Log.e("CommunityViewModel", "Failed to join community: ${e.message}")
                    }
            } else {
                Log.e("CommunityViewModel", "Community ID '$communityId' not found!")
            }
        }.addOnFailureListener { e ->
            Log.e("CommunityViewModel", "Error fetching community: ${e.message}")
        }
    }

    // Fetch a community by ID from Firestore
    fun getCommunityById(communityId: String, onComplete: (Community?) -> Unit) {
        db.collection("communities").document(communityId)
            .get()
            .addOnSuccessListener { document ->
                val community = document.toObject(Community::class.java)
                onComplete(community)
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    // Update an existing community
    fun updateCommunity(updatedCommunity: Community) {
        viewModelScope.launch {
            try {
                repository.updateCommunity(updatedCommunity)  // Update in repository
                _allCommunities.value = _allCommunities.value?.map {
                    if (it.id == updatedCommunity.id) updatedCommunity else it
                }  // Update the UI state
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error updating community: ${e.message}", e)
            }
        }
    }

    // Fetch all communities a user is a part of
    fun fetchAllCommunities() {
        viewModelScope.launch {
            try {
                _allCommunities.value = repository.getAllCommunities()
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error fetching all communities: ${e.message}", e)
            }
        }
    }

    // Fetch communities that a user has joined
    fun fetchJoinedCommunities(userId: String) {
        if (userId.isEmpty()) {
            Log.e("CommunityViewModel", "User ID is empty! Cannot fetch joined communities.")
            return
        }

        Firebase.firestore.collection("communities")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("CommunityViewModel", "Error fetching joined communities: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val communities = snapshots.toObjects(Community::class.java)
                    _joinedCommunities.postValue(communities)  // Post the updated list
                    Log.d("CommunityViewModel", "Real-time update: ${communities.size} joined communities")
                }
            }
    }

    // Refresh communities for a user
    fun refreshCommunities(userId: String) {
        viewModelScope.launch {
            try {
                _allCommunities.value = repository.getJoinedCommunities(userId)
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error refreshing communities: ${e.message}", e)
            }
        }
    }

    // Delete a community
    fun deleteCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                repository.deleteCommunity(communityId)
                _allCommunities.value = _allCommunities.value?.filterNot { it.id == communityId }  // Remove the deleted community from the list
                Log.d("CommunityViewModel", "Community deleted: $communityId")
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error deleting community: ${e.message}", e)
            }
        }
    }
    // Remove a member from a community
    fun removeMemberFromCommunity(communityId: String, userId: String) {
        val communityRef = db.collection("communities").document(communityId)

        communityRef.update("members", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                Log.d("CommunityViewModel", "User removed from community successfully")
                // Refresh the community after removing the user
                fetchAllCommunities()
            }
            .addOnFailureListener { e ->
                Log.e("CommunityViewModel", "Error removing user from community: ${e.message}")
            }
    }

    fun getCommunityMembers(communityId: String, onComplete: (List<String>?) -> Unit) {
        db.collection("communities").document(communityId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val community = document.toObject(Community::class.java)
                    onComplete(community?.members)
                } else {
                    Log.e("CommunityViewModel", "Community not found")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CommunityViewModel", "Error fetching community members: ${e.message}")
                onComplete(null)
            }
    }
}
