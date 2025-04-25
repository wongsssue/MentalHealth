package com.example.mentalhealthemotion.ui.theme

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.ChatbotViewModel
import com.example.mentalhealthemotion.Data.MessageChatbot
import com.example.mentalhealthemotion.Data.UserViewModel
import java.util.*
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic

@Composable
fun ChatbotScreen(
    navController: NavController,
    chatbotViewModel: ChatbotViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    var userMessage by remember { mutableStateOf("") }
    var lastSpokenMessage by remember { mutableStateOf("") }
    var isVoiceInput by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val user by userViewModel.currentUser.observeAsState()
    val chatHistory = chatbotViewModel.chatHistory
    var isRecording by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.e("Permission", "Microphone permission denied")
        }
    }

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            chatbotViewModel.loadChatHistory(userId.toString()) { /* Callback when data is loaded */ }
        }
    }


    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val textToSpeech = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("TTS", "Initialization failed!")
            }
        }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() { isRecording = true }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { isRecording = false }

        override fun onError(error: Int) {
            Log.e("SpeechRecognizer", "Error: $error")
            isRecording = false
        }

        override fun onResults(results: Bundle?) {
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                // If there's recognized speech, assign it to userMessage and send it to the chatbot
                userMessage = it
                chatbotViewModel.sendMessage(
                    userMessage = it,  // Use the recognized speech as the message
                    userId = (user?.userID ?: "guest").toString(),  // Default to guest if user ID is not found
                    fromVoice = true,  // Indicate that this is from voice input
                    onVoiceInputChange = { isVoiceInput = it },  // Update the voice input state
                    context = context, // Pass context for processing
                    onResponse = { response -> /* handle the response from chatbot */ }
                )
            }
            isRecording = false  // End voice recognition
        }



        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(recognitionListener)
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            speechRecognizer.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))  // Light Gray background
            .padding(16.dp)
    ) {
        if (chatHistory.isEmpty()) {
            Text(
                "Welcome to the chatbot! Ask me anything.",
                color = Color.Gray,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {
            items(chatHistory.toList()) { message ->
                ChatBubble(message)
            }
        }

        LaunchedEffect(chatHistory.size) {
            val lastMessage = chatHistory.lastOrNull()
            if (lastMessage?.role == "assistant" && lastMessage.content != lastSpokenMessage && isVoiceInput) {
                lastSpokenMessage = lastMessage.content
                textToSpeech.language = Locale.getDefault()
                textToSpeech.speak(lastMessage.content, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }

        Card(
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userMessage,
                    onValueChange = { userMessage = it },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { if (!isVoiceInput) Text("Type a message...") }
                )
                IconButton(
                    onClick = {
                        if (userMessage.isNotBlank()) {
                            // Call sendMessage when user input is not blank
                            chatbotViewModel.sendMessage(
                                userMessage,  // The user's message
                                userId = (user?.userID ?: "guest").toString(),  // User ID or guest
                                fromVoice = false,  // Set to false for text input
                                onVoiceInputChange = { isVoiceInput = it },  // Handle the voice input state change
                                context = context,  // Pass context for any required operations
                                onResponse = { response ->
                                    // Handle the chatbot's response here
                                    // For example, you can update the UI with the response
                                }
                            )
                            // Clear the user message after sending
                            userMessage = ""
                        }
                    }
                )

                {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFFFFA500)  // Orange Send Button
                    )
                }

                IconButton(
                    onClick = {
                        if (!isRecording && SpeechRecognizer.isRecognitionAvailable(context)) {
                            speechRecognizer.startListening(speechIntent)
                        } else {
                            speechRecognizer.stopListening()
                            isRecording = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isRecording) Color.Red else Color(0xFF4CAF50) // Green when inactive, Red when recording
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageChatbot) {
    val isUser = message.role == "user"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFF4CAF50) else Color(0xFF63B9FF), // Green for user, Purple for bot
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = Color.White,
                style = MaterialTheme.typography.body1
            )
        }
    }
}
