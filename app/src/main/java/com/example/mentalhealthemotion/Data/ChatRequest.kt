package com.example.mentalhealthemotion.Data

data class ChatRequest(
    val model: String,
    val messages: List<MessageChatbot>
)