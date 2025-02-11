package com.example.mentalhealthemotion.ui.theme

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.MusicViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R

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
    val context = LocalContext.current

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


    Box() {
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
                        .border(3.dp, shape = RoundedCornerShape(20.dp), color = Color(0xFFBEE4F4)),
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
            Spacer(modifier = Modifier.height(16.dp))

            // Playlist
            LazyColumn {
                itemsIndexed(songs) { index, track ->
                    MusicCard(
                        trackName = track.title,
                        isPlaying = track.isPlaying,
                        authorName = track.authorName,
                        duration = musicViewModel.formatDuration(track.durationInSeconds),
                        onTogglePlay = {
                            musicViewModel.togglePlay(context, track)
                        },
                        modifier = Modifier.padding(bottom = if (index == songs.lastIndex) 90.dp else 10.dp)
                    )
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

@Composable
fun MusicCard(
    trackName: String,
    isPlaying: Boolean,
    authorName: String,
    duration: String,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
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
                        .clickable(onClick = onTogglePlay),
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
    }
    Spacer(modifier = Modifier.height(10.dp))
}

