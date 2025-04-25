package com.example.mentalhealthemotion.Data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private var messageListener: ListenerRegistration? = null

    fun loadMessages(communityId: String) {
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(communityId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val messagesList = mutableListOf<Message>()

                snapshot.documents.forEach { doc ->
                    val mediaUrl = doc.getString("firebaseUrl")
                    val fileType = doc.getString("fileType")

                    messagesList.add(
                        Message(
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            firebaseUrl = mediaUrl,
                            fileType = fileType
                        )
                    )
                }
                _messages.postValue(messagesList) // Post final data
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Failed to load messages", it)
            }
    }

    fun listenForMessages(communityId: String) {
        messageListener?.remove()

        messageListener = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(communityId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error listening for messages", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val messagesList = mutableListOf<Message>()

                    it.documents.forEach { doc ->
                        val mediaUrl = doc.getString("firebaseUrl")
                        val fileType = doc.getString("fileType")

                        messagesList.add(
                            Message(
                                senderId = doc.getString("senderId") ?: "",
                                senderName = doc.getString("senderName") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                firebaseUrl = mediaUrl,
                                fileType = fileType
                            )
                        )
                    }

                    _messages.postValue(messagesList) // Update UI once
                }
            }
    }


    // Send a text message
    fun sendMessage(communityId: String, userId: String, userName: String?, text: String) {
        if (text.isBlank()) return

        val firestore = FirebaseFirestore.getInstance()
        val senderName = userName ?: "Unknown"

        val message = hashMapOf(
            "senderId" to userId,
            "senderName" to senderName,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("chats")
            .document(communityId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message sent successfully by $senderName")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error sending message", e)
            }
    }

    fun uploadMedia(
        context: Context,
        communityId: String,
        localUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Create storage reference
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(localUri)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "video/mp4" -> "mp4"
            else -> "dat"
        }

        val fileName = "${UUID.randomUUID()}.$extension"
        val storageRef = FirebaseStorage.getInstance().reference
            .child("chats/$communityId/$fileName")

        // Start uploading the file
        storageRef.putFile(localUri)
            .addOnSuccessListener {
                Log.d("Firebase", "Upload Success!") // Log to verify upload success
                // Now get the download URL
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.d("Firebase", "Download URL: $uri") // Log the download URL
                        onSuccess(uri.toString()) // Send the URL back
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Failed to fetch download URL: ${exception.message}")
                        onFailure(exception) // Call onFailure
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Upload Failed: ${exception.message}") // Log failure
                onFailure(exception) // Call onFailure
            }
    }



    fun sendMediaMessage(
        communityId: String,
        userId: String,
        userName: String,
        mediaUri: String,  // Firebase Storage URL after upload
        fileType: String
    ): String {
        val messageId = UUID.randomUUID().toString()
        val message = hashMapOf(
            "id" to messageId,
            "senderId" to userId,
            "senderName" to userName,
            "text" to "",
            "firebaseUrl" to mediaUri,
            "fileType" to fileType,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d("Firestore Path", "Sending message to: chats/$communityId/messages/$messageId")

        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(communityId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Failed to send media message", e)
            }

        return messageId
    }

    fun updateMessageWithUrl(
        communityId: String,
        messageId: String,
        firebaseUrl: String // Firebase URL after media upload
    ) {
        Log.d("Firestore Path", "Updating message with URL at: chats/$communityId/messages/$messageId")

        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(communityId)
            .collection("messages")
            .document(messageId)
            .update("firebaseUrl", firebaseUrl) // Correctly update the URL in Firestore
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Failed to update message with URL", e)
            }
    }


}