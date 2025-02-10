package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.R

@Composable
fun MusicPage(
    onNavigate: (String) -> Unit,
    toMusicStartPage: () -> Unit
) {
    var playlist by remember {
        mutableStateOf(
            listOf(
                "Daily Calm 3" to false,
                "Daily Calm 1" to false,
                "Daily Calm 2" to false
            )
        )
    }
    var isSortedAlphabetically by remember { mutableStateOf(false) }

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
                        .clickable { toMusicStartPage()  }
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
                        playlist = if (isSortedAlphabetically) {
                            playlist.sortedBy { it.first }
                        } else {
                            playlist.shuffled() // Example: Revert to unsorted (mock)
                        }
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
                items(playlist) { track ->
                    MusicCard(
                        trackName = track.first,
                        isPlaying = track.second,
                        onTogglePlay = {
                            playlist = playlist.map {
                                if (it.first == track.first) {
                                    it.first to !it.second
                                } else {
                                    it.first to false // Stop all other tracks
                                }
                            }
                        }
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
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier
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
                        painterResource(id = R.drawable.pause) // Replace with your drawable resource for pause
                    } else {
                        painterResource(id = R.drawable.play) // Replace with your drawable resource for play
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
                    text = "Author Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Duration: 3:30",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

