package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mentalhealthemotion.R

@Composable
fun QuizSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mental Health Quizzes",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val quizzes = listOf(
            QuizData("PHQ-9", R.drawable.phq, "Assess depression severity"),
            QuizData("DASS-21", R.drawable.dass, "Measure depression, anxiety, and stress")
        )

        quizzes.forEach { quiz ->
            QuizCard(quiz) {
                when (quiz.title) {
                    "PHQ-9" -> navController.navigate("phq9_screen")
                    "DASS-21" -> navController.navigate("dass_screen")
                }
            }
        }
    }
}

data class QuizData(val title: String, val imageRes: Int, val description: String)

@Composable
fun QuizCard(quiz: QuizData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = quiz.imageRes),
                contentDescription = quiz.title,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = quiz.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = quiz.description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
