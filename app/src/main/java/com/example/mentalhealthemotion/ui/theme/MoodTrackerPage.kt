package com.example.mentalhealthemotion.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.MoodEntry
import com.example.mentalhealthemotion.Data.MoodEntryViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R

@Composable
fun MoodTrackerPage(
    moodEntryViewModel: MoodEntryViewModel,
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    val moodEntries by moodEntryViewModel.moodEntries.observeAsState(emptyList())
    val user by userViewModel.currentUser.observeAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            moodEntryViewModel.loadMoodEntries(userId) // Load data when page opens
        }
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(150.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Mood Question Section
                    Text(
                        text = "How are you?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "awful" to R.drawable.awful,
                            "bad" to R.drawable.bad,
                            "meh" to R.drawable.meh,
                            "good" to R.drawable.good,
                            "rad" to R.drawable.rad
                        ).forEach { (mood, int) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable
                                {
                                    val moodType = moodEntryViewModel.toMoodType(mood)
                                    moodEntryViewModel.updateMood(moodType)
                                    onNavigate("EditEntryPage?isEditing=false")
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                ) {
                                    Image(
                                        painter = painterResource(int),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = mood, fontSize = 15.sp, color = Color.Black)
                            }
                        }
                    }

                }
            }
            // Mood Entry List
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = 150.dp)
            ) {
                if(moodEntries.isEmpty()){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(top = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You have not logged any mood yet.",
                            fontSize = 20.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else{
                    moodEntries.forEach { entry ->
                        entriesCard(
                            moodImg = getMoodDrawable(entry.moodType.name),
                            date = entry.date ?: "Unknown Date",
                            mood = entry.moodType.name,
                            activities = entry.activityName.joinToString(","),
                            moodEntry = entry,
                            userId = user?.userID ?: 0,
                            moodEntryViewModel = moodEntryViewModel,
                            onNavigate = onNavigate
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

fun getMoodDrawable(mood: String): Int {
    return when (mood.lowercase()) {
        "awful" -> R.drawable.awful
        "bad" -> R.drawable.bad
        "meh" -> R.drawable.meh
        "good" -> R.drawable.good
        "rad" -> R.drawable.rad
        else -> R.drawable.meh // Default to 'meh' if unknown
    }
}

@Composable
fun entriesCard(
    @DrawableRes moodImg: Int,
    date: String,
    mood: String,
    activities: String,
    moodEntry: MoodEntry,
    userId: Int,
    moodEntryViewModel: MoodEntryViewModel,
    onNavigate: (String) -> Unit
) {
    var showOptionsWindow by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp)
            ) {
                Image(
                    painter = painterResource(moodImg),
                    contentDescription = mood,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row {
                    Text(
                        text = "$date",
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more),
                        contentDescription = "More",
                        tint = Color.Black,
                        modifier = Modifier.clickable {
                            showOptionsWindow = true
                        }
                    )
                }
                Text(
                    text = "Mood: $mood",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Activities: $activities",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
    if (showOptionsWindow) {
        optionsWindow(
            moodEntryViewModel = moodEntryViewModel,
            moodEntry = moodEntry,
            userId = userId,
            onNavigate = onNavigate,
            onDismiss = { showOptionsWindow = false }
        )
    }
}

@Composable
fun optionsWindow(
    moodEntryViewModel: MoodEntryViewModel,
    moodEntry: MoodEntry,
    userId: Int,
    onNavigate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Card(
            modifier = Modifier
                .offset(y = -75.dp)
                .padding(top = 16.dp, bottom = 0.dp)
                .width(120.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Text(
                    text = "Edit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            moodEntryViewModel.setCurrentMoodEntry(moodEntry)
                            onNavigate("EditEntryPage?isEditing=true")
                            onDismiss()
                        },
                    textAlign = TextAlign.Center
                )
                Divider(color = Color.LightGray, thickness = 1.dp)
                Text(
                    text = "Delete",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            moodEntryViewModel.deleteMoodEntry(userId, moodEntry)
                            onDismiss()
                        },
                    textAlign = TextAlign.Center
                )
                Divider(color = Color.LightGray, thickness = 1.dp)
                Text(
                    text = "Cancel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable {
                            moodEntryViewModel.setCurrentMoodEntry(null)
                            onDismiss()
                                   },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class BottomNavItem(val label: String, @DrawableRes val icon: Int, val route: String)

@Composable
fun BottomNavigationBar(onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    val items = listOf(
        BottomNavItem("Entries", R.drawable.entries, "MoodTrackerPage"),
        BottomNavItem("Stats", R.drawable.stats, "StatisticPage"),
        BottomNavItem("Music", R.drawable.music, "MusicStartPage"),
        BottomNavItem("Sleep Test", R.drawable.test, "QuestionnairePage"),
        BottomNavItem("Edu Library", R.drawable.library, "EduLibraryPage")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .shadow(5.dp, shape = RoundedCornerShape(2.dp))
            .background(Color(0xFFBEE4F4)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    onNavigate(item.route)
                }
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.label,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = item.label,
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

