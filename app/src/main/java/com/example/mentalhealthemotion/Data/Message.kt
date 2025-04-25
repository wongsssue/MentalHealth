package com.example.mentalhealthemotion.Data

data class Message(
    val id: String = "",  // Unique message ID for updating media URLs
    val senderId: String = "",
    val senderName: String = "",  // Store the actual user name
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val firebaseUrl: String? = null,  // URL for media files (image, video, voice)
    val fileType: String? = null   // Type of media: "image", "video", "voice"
)
