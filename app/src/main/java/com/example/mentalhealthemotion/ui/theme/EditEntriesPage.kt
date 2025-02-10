package com.example.mentalhealthemotion.ui.theme

import android.Manifest
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import com.example.mentalhealthemotion.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import com.example.mentalhealthemotion.Data.MoodEntryViewModel
import com.example.mentalhealthemotion.Data.MoodType
import com.example.mentalhealthemotion.Data.UserViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditEntryPage(
    userViewModel: UserViewModel,
    moodEntryViewModel: MoodEntryViewModel,
    isEditing: Boolean,
    onback: () -> Unit
) {
    val user by userViewModel.currentUser.observeAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val moodEntry by moodEntryViewModel.currentMoodEntry.observeAsState()
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    val audioAttachment by moodEntryViewModel.audioAttachment
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    if (!permissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(moodEntry) {
        if (isEditing) {
            moodEntry?.let { entry ->
                moodEntryViewModel.updateMood(entry.moodType)
                moodEntryViewModel.setSelectedActivities(entry.activityName)
                moodEntryViewModel.updateQuickNote(entry.note)
                val dateToUpdate = entry.date ?: moodEntryViewModel.generateCurrentDate()
                moodEntryViewModel.updateDate(dateToUpdate)
                moodEntryViewModel.updateAudio(entry.audioAttachment?: "No audio recorded")
                val (date, duration) = moodEntryViewModel.getAudioFileDetails(context, entry.audioAttachment?: "No audio recorded")
                moodEntryViewModel.setAudioDetails(date, duration)
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Cancel",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                moodEntryViewModel.setCurrentMoodEntry(null)
                moodEntryViewModel.clearFields()
                onback()
            }
        )
        // Mood Selection Card
        MoodSelectionCard(moodEntryViewModel, moodEntryViewModel.selectedMood.value)
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Activity Selection Section
        Text(
            text = "Select Activities",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E3E64)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActivitySelectionCard(moodEntryViewModel.selectedActivities.value) { selectedActivity ->
            moodEntryViewModel.toggleActivity(selectedActivity)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Quick Note Section
        Text(
            text = "Quick Note",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E3E64)
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = moodEntryViewModel.quickNote.value,
            onValueChange = { moodEntryViewModel.updateQuickNote(it) },
            placeholder = { Text("Add Note...") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(0xFFF5F5F5))
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Audio Recording Section
        Text(
            text = "Record Audio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E3E64)
        )
        Spacer(modifier = Modifier.height(10.dp))
        AudioRecorder(
            isRecording,
            onStartRecording = {
                if (permissionState.status.isGranted) {
                    moodEntryViewModel.startRecording(context)
                    isRecording = true
                } else {
                    permissionState.launchPermissionRequest()
                }
            },
            onStopRecording = {
                val recordedAudioPath = moodEntryViewModel.stopRecording(context) // Pass context
                isRecording = false

                if (recordedAudioPath.isNotEmpty()) {
                    moodEntryViewModel.updateAudio(recordedAudioPath)
                    val (date, duration) = moodEntryViewModel.getAudioFileDetails(context, recordedAudioPath)
                    moodEntryViewModel.setAudioDetails(date, duration)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
          AudioPlayer(
            isPlaying = isPlaying,
            onPlayPause = {
               val audioPath = audioAttachment
               if (audioPath != null) {
                   if (isPlaying) {
                       moodEntryViewModel.stopAudio()
                   } else {
                       moodEntryViewModel.playAudio(audioPath)
                   }
                   isPlaying = !isPlaying
               } else {
                   Toast.makeText(context, "No audio available", Toast.LENGTH_SHORT).show()
               }
           },
           audioDuration = moodEntryViewModel.audioDetails.value.second,
           currentDate = moodEntryViewModel.audioDetails.value.first
       )

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))
        // Update Button
        if (isEditing) {
            Button(
                onClick = {
                    user?.userID?.let { userId ->
                        val currentMoodEntry = moodEntry ?: return@let
                        moodEntryViewModel.updateDate(moodEntryViewModel.generateCurrentDate())

                        moodEntryViewModel.updateMoodEntry(
                            userId,
                            currentMoodEntry.copy(
                                moodType = moodEntryViewModel.selectedMood.value,
                                activityName = moodEntryViewModel.selectedActivities.value,
                                note = moodEntryViewModel.quickNote.value,
                                date = moodEntryViewModel.selectedDate.value
                            ),
                            moodEntryViewModel.audioAttachment.value,
                            onSuccess = { onback() }
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(120.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF50C878))
            ) {
                Text(text = "Update", fontSize = 20.sp, color = Color.White)
            }
        } else {
            //Save Button
            Button(
                onClick = {
                    user?.let {
                        moodEntryViewModel.addMoodEntry(
                            context,
                            it.userID,
                            onSuccess = { onback() })
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(120.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF50C878))
            ) {
                Text(text = "Save", fontSize = 20.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AudioRecorder(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(4.dp, Color(0xFFBEE4F4), CircleShape)
                        .clickable { if (!isRecording) onStartRecording() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.audio),
                        contentDescription = "Start Recording Button",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(if (isRecording) 0.4f else 1.0f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stop Recording Button
                Button(
                    onClick = { onStopRecording() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBEE4F4)),
                    enabled = isRecording,
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .height(45.dp)
                        .width(180.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 5.dp)
                ) {
                    Text(
                        text = "Stop Recording",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPlayer(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    audioDuration: String,
    currentDate: String
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(90.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
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
                    .border(3.dp, Color(0xFFBEE4F4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isPlaying) {
                        painterResource(id = R.drawable.pause)
                    } else {
                        painterResource(id = R.drawable.play)
                    },
                    modifier = Modifier
                        .size(35.dp)
                        .clickable { onPlayPause() },
                    contentDescription = if (isPlaying) "Pause Icon" else "Play Icon",
                    tint = Color(0xFFBEE4F4)
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            Column {
                Text(
                    text = currentDate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3E64)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Duration: $audioDuration",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun MoodSelectionCard(
    moodEntryViewModel: MoodEntryViewModel,
    selectedMood: MoodType
) {
    val moods = listOf(
        MoodType.awful to R.drawable.awful,
        MoodType.bad to R.drawable.bad,
        MoodType.meh to R.drawable.meh,
        MoodType.good to R.drawable.good,
        MoodType.rad to R.drawable.rad
    )

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(150.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = moodEntryViewModel.selectedDate.value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E3E64),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(moods) { (mood, icon) ->
                    MoodItem(
                        mood = mood,
                        icon = icon,
                        selectedMood = selectedMood,
                        onMoodSelected = { moodEntryViewModel.updateMood(mood) }
                    )
                }
            }
        }
    }
}

@Composable
fun MoodItem(
    mood: MoodType,
    @DrawableRes icon: Int,
    selectedMood: MoodType,
    onMoodSelected: (MoodType) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onMoodSelected(mood) }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = mood.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (selectedMood == mood) 1f else 0.4f)
            )
        }
        Text(text = mood.name, fontSize = 12.sp, color = Color.Black)
    }
}


@Composable
fun ActivitySelectionCard(
    selectedActivities: List<String>,
    onActivityToggled: (String) -> Unit
) {
    val activities = listOf(
        "gym", "reading", "family", "music",
        "movies", "eating", "friends", "date",
        "sleep", "swimming", "shopping", "cleaning",
        "relax", "gaming", "fishing", "surfing"
    )
    val icons = listOf(
        R.drawable.gym, R.drawable.reading, R.drawable.family, R.drawable.music,
        R.drawable.movies, R.drawable.eating, R.drawable.friends, R.drawable.date,
        R.drawable.sleeping, R.drawable.swimming, R.drawable.shopping, R.drawable.cleaning,
        R.drawable.relax, R.drawable.gaming, R.drawable.fishing, R.drawable.surfing
    )

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            activities.zip(icons).chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    rowItems.forEach { (activity, icon) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onActivityToggled(activity) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                            ) {
                                Image(
                                    painter = painterResource(icon),
                                    contentDescription = activity,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (selectedActivities.contains(activity)) 1f else 0.4f)
                                )
                            }
                            Text(text = activity, fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
