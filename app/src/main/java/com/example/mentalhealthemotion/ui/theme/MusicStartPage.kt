package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.MoodEntryViewModel


@Composable
fun MusicStartPage(
    onNavigate: (String) -> Unit,
    musicSuggestions: () -> Unit,
    moodEntryViewModel: MoodEntryViewModel
) {

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.Transparent, shape = CircleShape)
                    .border(4.dp, Color(0xFFBEE4F4), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Icon",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFBEE4F4)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "MUSIC",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E3E64),
                modifier = Modifier.padding(bottom = 20.dp)
            )
            Text(
                text = "Let's boost your mood through music!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    musicSuggestions()
                    moodEntryViewModel.clearOverrideMood()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBEE4F4)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .height(50.dp)
                    .width(150.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
            ) {
                Text(
                    text = "Start Now",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
        // Bottom Navigation Bar
        BottomNavigationBar(
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}