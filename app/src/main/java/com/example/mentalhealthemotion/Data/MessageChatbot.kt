package com.example.mentalhealthemotion.Data

data class MessageChatbot(
    val userId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long // Use Long to store Firebase Timestamp as milliseconds
)
