package com.example.mentalhealthemotion.Data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Timestamp

class ChatbotViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val chatbotApi = RetrofitClient.instance.create(ChatbotApi::class.java)

    var chatHistory = mutableStateListOf<MessageChatbot>()
        private set

    var messages = mutableListOf<MessageChatbot>()

    // Load chat history from Firestore
    fun loadChatHistory(userId: String, onComplete: () -> Unit) {
        db.collection("chatbot_conversations")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val messagesList = document.get("messages") as? List<HashMap<String, Any>> ?: emptyList()
                    chatHistory.clear()
                    chatHistory.addAll(messagesList.map { map ->
                        MessageChatbot(
                            userId = map["userId"] as? String ?: "unknown",
                            role = map["role"] as? String ?: "user",
                            content = map["content"] as? String ?: "",
                            timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
                        )
                    })
                }
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("ChatbotViewModel", "Error loading chat history", e)
            }
    }

    // Send a message and get a response from the chatbot API
    fun sendMessage(
        userMessage: String,
        userId: String,
        fromVoice: Boolean = false,
        onVoiceInputChange: (Boolean) -> Unit = {},
        context: Context? = null,
        onResponse: (String) -> Unit
    ) {
        val timestamp = System.currentTimeMillis()
        val userMsg = MessageChatbot(userId, "user", userMessage, timestamp)
        chatHistory.add(userMsg)

        val request = ChatRequest("mistralai/Mistral-Nemo-Instruct-2407", chatHistory)

        chatbotApi.sendMessage(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val botReply = response.body()?.choices?.firstOrNull()?.message?.content ?: "I didn't understand."
                    val botMsg = MessageChatbot(userId, "assistant", botReply, System.currentTimeMillis())
                    chatHistory.add(botMsg)

                    Log.d("Chatbot", "Saving conversation to Firestore...")
                    saveConversationToFirestore(userId, chatHistory) // Ensure this is called

                    onResponse(botReply)
                } else {
                    onResponse("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                onResponse("Error: ${t.message}")
            }
        })
    }


    // Save chat history to Firestore
    private fun saveConversationToFirestore(userId: String, messages: List<MessageChatbot>) {
        val userDocRef = db.collection("chatbot_conversations").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            val existingMessages = if (document.exists()) {
                val messagesList = document.get("messages") as? List<HashMap<String, Any>> ?: emptyList()
                messagesList.map { map ->
                    MessageChatbot(
                        userId = map["userId"] as? String ?: "unknown",
                        role = map["role"] as? String ?: "user",
                        content = map["content"] as? String ?: "",
                        timestamp = map["timestamp"] as? Long ?: System.currentTimeMillis()
                    )
                }
            } else {
                emptyList()
            }

            val updatedMessages = existingMessages + messages

            userDocRef.set(
                mapOf("messages" to updatedMessages.map { it.toMap() }, "timestamp" to System.currentTimeMillis())
            ).addOnSuccessListener {
                Log.d("Firestore", "Conversation updated successfully")
            }.addOnFailureListener {
                Log.e("Firestore", "Error updating conversation", it)
            }
        }.addOnFailureListener {
            Log.e("Firestore", "Error retrieving existing conversation", it)
        }
    }

    // Convert MessageChatbot object to Firestore-compatible map
    private fun MessageChatbot.toMap(): Map<String, Any> {
        return mapOf(
            "userId" to this.userId,
            "role" to this.role,
            "content" to this.content,
            "timestamp" to this.timestamp
        )
    }
}

