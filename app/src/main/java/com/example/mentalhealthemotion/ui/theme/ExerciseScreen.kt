package com.example.mentalhealthemotion.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.R
import kotlinx.coroutines.delay

@Composable
fun ExerciseScreen(context: Context) {
    var selectedExercise by remember { mutableStateOf("breathing") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectedExercise = "breathing" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedExercise == "breathing") Color.Blue else Color.LightGray
                )
            ) {
                Text("Breathing Exercise", color = Color.White)
            }
            Button(
                onClick = { selectedExercise = "yoga" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedExercise == "yoga") Color.Blue else Color.LightGray
                )
            ) {
                Text("Yoga Tracker", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedExercise == "breathing") {
            BreathingScreen(context)
        } else {
            YogaExerciseScreen(context)
        }
    }
}


@Composable
fun BreathingScreen(context: Context) {
    var isPlaying by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(5) }
    var remainingTime by remember { mutableStateOf(selectedTime * 60) }
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.relax_music) }
    val tts = remember { TextToSpeech(context) { } }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                tween(4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
    } else {
        remember { mutableStateOf(1f) } // Reset scale when stopped
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                tts.speak("Breathe in... Hold... Breathe out", TextToSpeech.QUEUE_FLUSH, null, null)
            }

            while (remainingTime > 0 && isPlaying) {
                delay(1000L)
                remainingTime--
            }

            if (remainingTime == 0) {
                isPlaying = false
                mediaPlayer.pause()
                mediaPlayer.seekTo(0) // Reset only when the timer finishes
            }
        } else {
            mediaPlayer.pause() // Just pause instead of resetting
        }
    }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isPlaying) "Relax and Breathe..." else "Start Meditation",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size((150 * animatedScale).dp)) {
                    drawCircle(Color(0xFF0288D1))
                }
                Text(
                    text = "${remainingTime / 60}:${String.format("%02d", remainingTime % 60)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(5, 10, 15).forEach { time ->
                    Button(
                        onClick = {
                            selectedTime = time
                            remainingTime = time * 60
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedTime == time) Color.Blue else Color.LightGray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("$time min", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isPlaying = !isPlaying
                    if (!isPlaying) {
                        mediaPlayer.pause() // Pause instead of resetting
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = if (isPlaying) Color.Red else Color.Green),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (isPlaying) "Pause" else "Start", color = Color.White, fontSize = 18.sp)
            }
        }
    }


@Composable
fun YogaExerciseScreen(context: Context) {
    val yogaImages = (1..19).map { "yoga$it" }
    var currentIndex by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(10) }
    var isPaused by remember { mutableStateOf(false) }
    var isBreakTime by remember { mutableStateOf(false) }
    var isInSelectionMode by remember { mutableStateOf(true) }

    val tts = remember { TextToSpeech(context) {} }

    if (isInSelectionMode) {
        // Yoga Pose Selection Grid
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text("Select a Yoga Pose", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f)
            ) {
                items(yogaImages.size) { index ->
                    val resId = context.resources.getIdentifier(yogaImages[index], "drawable", context.packageName)
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Pose ${index + 1}",
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .clickable {
                                currentIndex = index
                                isInSelectionMode = false
                                isPaused = false
                                countdown = 10
                            }
                    )
                }
            }
        }
    } else {
        // Yoga Timer Mode
        LaunchedEffect(currentIndex, isPaused, isBreakTime) {
            while (!isPaused) {
                if (isBreakTime) {
                    tts.speak("Ready, next step coming...", TextToSpeech.QUEUE_FLUSH, null, null)
                    for (i in countdown downTo 1) {
                        countdown = i
                        delay(1000L)
                        if (isPaused) return@LaunchedEffect
                    }
                    isBreakTime = false
                    if (currentIndex < yogaImages.size - 1) {
                        currentIndex++
                        countdown = 10
                    }
                } else {
                    for (i in countdown downTo 1) {
                        countdown = i
                        tts.speak("$i", TextToSpeech.QUEUE_FLUSH, null, null)
                        delay(1000L)
                        if (isPaused) return@LaunchedEffect
                    }
                    isBreakTime = true
                    countdown = 5
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${currentIndex + 1} / ${yogaImages.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Image(
                painter = painterResource(id = context.resources.getIdentifier(yogaImages[currentIndex], "drawable", context.packageName)),
                contentDescription = "Yoga Pose",
                modifier = Modifier.size(300.dp)
            )

            Text(
                text = if (isBreakTime) "Break: $countdown sec" else "Countdown: $countdown sec",
                fontSize = 24.sp
            )

            Row {
                Button(
                    onClick = { isPaused = !isPaused },
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (isPaused) Color.Green else Color.Red),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(if (isPaused) "Resume" else "Pause", color = Color.White)
                }

                Button(
                    onClick = {
                        isPaused = true
                        currentIndex = 0
                        countdown = 10
                        isBreakTime = false
                        isInSelectionMode = true
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Back to Select", color = Color.White)
                }
            }
        }
    }
}
