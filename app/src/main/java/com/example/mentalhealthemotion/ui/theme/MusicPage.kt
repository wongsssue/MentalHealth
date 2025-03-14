package com.example.mentalhealthemotion.ui.theme

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mentalhealthemotion.Data.MusicViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import android.content.Context as Context

@Composable
fun MusicPage(
    onNavigate: (String) -> Unit,
    toMusicStartPage: () -> Unit,
    musicViewModel: MusicViewModel,
    userViewModel: UserViewModel
) {
    val user by userViewModel.currentUser.observeAsState()
    val latestMood by musicViewModel.moodEntry.observeAsState()
    var isSortedAlphabetically by remember { mutableStateOf(false) }
    val songs by musicViewModel.songs.collectAsState()

    //val showCheckCalmness = latestMood?.moodType in listOf(MoodType.awful, MoodType.bad)

    val context = LocalContext.current
   /* val lifecycleOwner = LocalContext.current as LifecycleOwner
    val heartRate by musicViewModel.heartRate.observeAsState()
    val isMeasuring = remember { mutableStateOf(false) }*/

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            musicViewModel.getLatestMoodEntry(userId)
        }
    }

    LaunchedEffect(latestMood) {
        latestMood?.let { moodEntry ->
            musicViewModel.loadSongsForMood(moodEntry.moodType)
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row{
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back",
                    tint = Color(0xFF2E3E64),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { toMusicStartPage() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Music Suggested for You",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3E64),
                    modifier = Modifier.padding(bottom = 30.dp)
                )
            }
            // Message when no mood has been logged
            if (latestMood == null) {
                Text(
                    text = "Please log your mood first to get music recommendations.",
                    fontSize = 18.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 180.dp)
                )
            } else {
                // Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            isSortedAlphabetically = !isSortedAlphabetically
                            musicViewModel.sortSongs(isSortedAlphabetically)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(horizontal = 15.dp)
                            .height(50.dp)
                            .width(150.dp)
                            .border(
                                3.dp,
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFBEE4F4)
                            ),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.filter),
                                modifier = Modifier.size(25.dp),
                                contentDescription = "Filter Icon",
                                tint = Color(0xFF66AEDD)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSortedAlphabetically) "Shuffle" else "Sort A-Z",
                                color = Color(0xFF2E3E64),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(14.dp))
                /*
                if (showCheckCalmness) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Heart Rate Display
                        Text(
                            text = buildAnnotatedString {
                                append("Heart Rate: ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                    append(heartRate.toString())
                                }
                                append(" BPM")
                            },
                            fontSize = 15.sp,
                            color = Color(0xFF2E3E64),
                            fontWeight = FontWeight.Normal
                        )
                        // "Check Calmness" button for negative moods
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    if (isMeasuring.value) {
                                        isMeasuring.value = false
                                        musicViewModel.stopMeasurement()
                                    } else {
                                        if (checkCameraPermission(context)) {
                                            isMeasuring.value = true
                                            musicViewModel.startMeasurement(context, lifecycleOwner)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(
                                        0xFFBEE4F4
                                    )
                                )
                            ) {
                                Text(
                                    if (isMeasuring.value) "Reset" else "Check Calmness",
                                    color = Color.White
                                )
                            }

                            // Show message when measurement is active
                            if (isMeasuring.value) {
                                Text(
                                    text = "Place one of your fingers on the camera lens.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))*/
                // Playlist
                LazyColumn {
                    itemsIndexed(songs) { index, track ->
                        MusicCard(
                            trackName = track.title,
                            isPlaying = track.isPlaying,
                            authorName = track.authorName,
                            duration = musicViewModel.formatDuration(track.durationInSeconds),
                            youtubeVideoId = track.videoId,
                            onTogglePlay = {
                                musicViewModel.togglePlay(track)
                            },
                            modifier = Modifier.padding(bottom = if (index == songs.lastIndex) 90.dp else 10.dp)
                        )
                    }

                }
            }

        }

        // Bottom Navigation Bar
        BottomNavigationBar(
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


private fun checkCameraPermission(context: Context): Boolean {
    return if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        true
    } else {
        ActivityCompat.requestPermissions(
            (context as Activity),
            arrayOf(Manifest.permission.CAMERA),
            100
        )
        false
    }
}

@Composable
fun MusicCard(
    trackName: String,
    isPlaying: Boolean,
    authorName: String,
    duration: String,
    youtubeVideoId: String,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlayer by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Transparent, shape = CircleShape)
                        .border(4.dp, Color(0xFFBEE4F4), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = if (isPlaying) {
                            painterResource(id = R.drawable.pause)
                        } else {
                            painterResource(id = R.drawable.play)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                onTogglePlay()
                                showPlayer = !showPlayer
                            },
                        contentDescription = if (isPlaying) "Pause Icon" else "Play Icon",
                        tint = Color(0xFFBEE4F4)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = trackName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E3E64)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Author: $authorName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Duration: $duration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            // Show YouTube Player when playing
            if (showPlayer) {
                PlayYouTubeVideoDialog(videoId = youtubeVideoId,  onDismiss = { showPlayer = false })
            }
        }
    }
}

@Composable
fun PlayYouTubeVideoDialog(videoId: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.Black)
        ) {
            val lifecycleOwner = LocalLifecycleOwner.current
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        lifecycleOwner.lifecycle.addObserver(this)
                        enableAutomaticInitialization = false

                        initialize(object : YouTubePlayerListener {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadVideo(videoId, 0f)
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                Log.e("YouTubePlayer", "Error: $error")
                            }

                            override fun onApiChange(youTubePlayer: YouTubePlayer) {}
                            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {}
                            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
                            override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {}
                            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
                            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
                            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
                        })
                    }
                }
            )

            // Close button (optional)
            IconButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}


