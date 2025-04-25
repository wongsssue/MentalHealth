package com.example.mentalhealthemotion.ui.theme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mentalhealthemotion.Data.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.MediaItem
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.mentalhealthemotion.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    communityId: String,
    userId: String,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel
) {
    val messages by chatViewModel.messages.observeAsState(emptyList())
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var messageText by remember { mutableStateOf("") }
    val user by userViewModel.currentUser.observeAsState()
    val userName = remember(user) { user?.userName ?: "Unknown" }
    val firestore = remember { FirebaseFirestore.getInstance() }
    var communityName by remember { mutableStateOf("Community") }
    var isTyping by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var showBreathingDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val motivationalMessages = listOf(
        "You're stronger than you think! 💪",
        "Take a deep breath, you've got this! 🌿",
        "Every day is a fresh start! ☀️",
        "Believe in yourself, you are amazing! ✨"
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Log.e("ChatScreen", "Recording permission denied")
    }

    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }

    // Load community data
    LaunchedEffect(communityId) {
        firestore.collection("communities").document(communityId).get()
            .addOnSuccessListener { document ->
                communityName = document.getString("name") ?: "Community"
            }
            .addOnFailureListener { communityName = "Community" }
        chatViewModel.loadMessages(communityId)
        chatViewModel.listenForMessages(communityId)
    }

    LaunchedEffect(userId) { userViewModel.fetchCurrentUser(userId) }
// Image Picker
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Upload the selected image to Firebase Storage first
            chatViewModel.uploadMedia(
                context = context,
                communityId = communityId,
                localUri = uri,
                onSuccess = { uploadedUrl ->
                    // Log the uploaded URL to make sure it’s not empty
                    Log.d("ChatScreen", "Uploaded URL: $uploadedUrl")

                    // Once the upload is successful, send the message with the Firebase URL
                    chatViewModel.sendMediaMessage(
                        communityId = communityId,
                        userId = userId,
                        userName = userName,
                        mediaUri = uploadedUrl, // Set the uploaded URL
                        fileType = "image"
                    )
                },
                onFailure = { e ->
                    Log.e("ChatScreen", "Failed to upload media: ${e.message}")
                }
            )
        }
    }

// Video Picker
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("VideoPicker", "Picked video URI: $it")

            try {
                // Get the InputStream from the URI
                val inputStream = context.contentResolver.openInputStream(it)

                // Validate InputStream is not null
                if (inputStream == null) {
                    Log.e("ChatScreen", "Failed to open input stream for URI: $it")
                    return@let
                }

                // Create a Firebase Storage reference for the video upload
                val storageRef = FirebaseStorage.getInstance().reference.child("chats/$communityId/${UUID.randomUUID()}.mp4")

                // Upload the video stream to Firebase Storage
                storageRef.putStream(inputStream)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            Log.d("ChatScreen", "Uploaded URL: $downloadUri")

                            // Send the uploaded URL in the message
                            chatViewModel.sendMediaMessage(
                                communityId = communityId,
                                userId = userId,
                                userName = userName,
                                mediaUri = downloadUri.toString(),  // Set the uploaded URL
                                fileType = "video"
                            )
                        }.addOnFailureListener { e ->
                            Log.e("ChatScreen", "Failed to retrieve download URL: ${e.message}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatScreen", "Failed to upload video: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error opening input stream: ${e.message}")
            }
        }
    }


    // Voice recording
    val voiceRecorder = remember { VoiceRecorder(context) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Text(text = communityName,
                color = Color.Black,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4A90E2), Color(0xFF50C9C3))
                        ))
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState
                ) {
                    items(messages) { message ->
                        ChatBubble(message, isMe = message.senderId == userId)
                    }
                }

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.Image, contentDescription = "Send Image")
                        }
                        IconButton(onClick = { videoPicker.launch("video/*") }) {
                            Icon(Icons.Default.VideoCameraFront, contentDescription = "Send Video")
                        }
                        IconButton(onClick = {
                            if (isRecording) {
                                voiceRecorder.stopRecording { fileUri ->
                                    val tempMessageId = chatViewModel.sendMediaMessage(
                                        communityId = communityId,
                                        userId = userId,
                                        userName = userName,
                                        mediaUri = "", // empty for now
                                        fileType = "voice"
                                    )

                                    chatViewModel.uploadMedia(
                                        context = context,
                                        communityId = communityId,
                                        localUri = fileUri,
                                        onSuccess = { uploadedUrl ->
                                            chatViewModel.updateMessageWithUrl(
                                                communityId = communityId,
                                                messageId = tempMessageId.toString(),
                                                firebaseUrl = uploadedUrl
                                            )
                                        },
                                        onFailure = { e ->
                                            Log.e("ChatScreen", "Failed to upload voice: ${e.message}")
                                        }
                                    )
                                }
                            } else {
                                voiceRecorder.startRecording()
                            }
                            isRecording = !isRecording
                        }) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Record Voice",
                                tint = if (isRecording) Color.Red else Color(0xFF4CAF50)
                            )
                        }

                        TextField(
                            value = messageText,
                            onValueChange = {
                                messageText = it
                                isTyping = messageText.isNotEmpty()
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text("Type a message...") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        val coroutineScope = rememberCoroutineScope()
                        if (showBreathingDialog) {
                            BreathingDialog(onDismiss = { showBreathingDialog = false })
                        }

                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    coroutineScope.launch {
                                        try {
                                            val sentiment = analyzeSentiment(
                                                messageText,
                                                "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"
                                            )
                                            chatViewModel.sendMessage(
                                                communityId,
                                                userId,
                                                userName,
                                                messageText
                                            )
                                            messageText = ""
                                            if ("NEGATIVE" in sentiment) {
                                                val randomMessage = motivationalMessages.random()
                                                val result = snackbarHostState.showSnackbar(
                                                    message = randomMessage,
                                                    actionLabel = "Breathe",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) showBreathingDialog =
                                                    true
                                            }
                                        } catch (e: Exception) {
                                            Log.e(
                                                "SentimentError",
                                                "Error analyzing sentiment: ${e.message}",
                                                e
                                            )
                                        }
                                    }
                                }
                            },
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Send",
                                tint = Color(0xFFFFA500)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = autoPlay
        }
    }

    // Update media when videoUrl changes
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    // Release player when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
        modifier = modifier
    )
}


@Composable
fun ChatBubble(message: Message, isMe: Boolean) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(1) } // Prevent division by zero

    fun playAudio(url: String) {
        if (isPlaying) {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        } else {
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener {
                    duration = it.duration.coerceAtLeast(1)
                    start()
                    isPlaying = true
                }
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                    isPlaying = false
                    progress = 0f
                }
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(500)
            mediaPlayer?.let {
                progress = it.currentPosition / duration.toFloat()
            }
        }
    }

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(message.timestamp)

    val bubbleColor = if (isMe) Color(0xFFDCF8C6) else Color(0xFFF0F0F0)
    val alignment = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        horizontalAlignment = alignment
    ) {
        if (!isMe && message.senderName != null) {
            Text(
                text = message.senderName,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Card(
            shape = RoundedCornerShape(
                topStart = if (isMe) 16.dp else 0.dp,
                topEnd = if (isMe) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(bubbleColor),
            modifier = Modifier.widthIn(min = 100.dp, max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                when (message.fileType) {
                    "voice" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { playAudio(message.firebaseUrl ?: "") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Play Voice",
                                    tint = Color.Black
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                LinearProgressIndicator(
                                    progress = progress,
                                    color = Color(0xFF4A90E2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${mediaPlayer?.currentPosition?.div(1000) ?: 0}s / ${duration / 1000}s",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    "image" -> {
                        AsyncImage(
                            model = message.firebaseUrl,
                            contentDescription = "Sent Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    "video" -> {
                        val exoPlayer = remember {
                            ExoPlayer.Builder(context).build().apply {
                                setMediaItem(MediaItem.fromUri(message.firebaseUrl ?: ""))
                                prepare()
                            }
                        }

                        AndroidView(
                            factory = {
                                PlayerView(it).apply {
                                    player = exoPlayer
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        400
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    else -> {
                        Text(message.text, fontSize = 16.sp, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}



suspend fun analyzeSentiment(text: String, apiKey: String): String {
    val client = OkHttpClient()
    val requestBody = """{"inputs": "$text"}"""
    val request = Request.Builder()
        .url("https://api-inference.huggingface.co/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english")
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Content-Type", "application/json")
        .post(RequestBody.create("application/json".toMediaType(), requestBody))
        .build()

    return withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@withContext "Error: Empty response"
                    Log.d("SentimentDebug", "Raw API response: $responseBody")

                    // Fix: Handle nested JSON array properly
                    val jsonArray = JSONArray(responseBody)
                    if (jsonArray.length() > 0) {
                        val sentimentArray = jsonArray.getJSONArray(0)  // Extract inner array
                        val topResult = sentimentArray.getJSONObject(0)  // Get first object
                        val label = topResult.getString("label")
                        val score = topResult.getDouble("score")

                        return@withContext "$label ($score)"
                    }
                    return@withContext "Error: Unexpected response format"
                } else {
                    throw IOException("Unexpected response: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("SentimentError", "Error analyzing sentiment: ${e.message}")
            return@withContext "Error analyzing sentiment"
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun BreathingDialog(onDismiss: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }
    val context = LocalContext.current
    val mediaPlayer by remember { mutableStateOf(MediaPlayer.create(context, R.raw.relax_music)) }

    val progress by animateFloatAsState(
        targetValue = timeLeft / 60f,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isPlaying = false
            mediaPlayer.pause()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4A90E2), Color(0xFF50C9C3))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Take a Deep Breath",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Relax and focus on your breath",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = progress,
                        strokeWidth = 8.dp,
                        color = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        "$timeLeft",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            mediaPlayer.start()
                            timeLeft = 60
                        } else {
                            mediaPlayer.pause()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF4A90E2)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .shadow(4.dp, shape = RoundedCornerShape(10.dp))
                ) {
                    Text(if (isPlaying) "Pause" else "Start", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                    onDismiss()
                }) {
                    Text("Close", color = Color.White.copy(alpha = 0.9f))
                }
            }
        }
    }
}




