package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.QuizViewModel
import com.example.mentalhealthemotion.Data.UserViewModel

@Composable
fun DASS21Screen(navController: NavController) {
    var totalScore by remember { mutableStateOf(0) }
    var depressionScore by remember { mutableStateOf(0) }
    var anxietyScore by remember { mutableStateOf(0) }
    var stressScore by remember { mutableStateOf(0) }

    val questions = listOf(
        "I found it hard to wind down",  // Depression
        "I was aware of dryness of my mouth",  // Anxiety
        "I couldn’t seem to experience any positive feeling at all",  // Depression
        "I experienced breathing difficulty",  // Anxiety
        "I found it difficult to work up the initiative to do things",  // Depression
        "I tended to over-react to situations",  // Stress
        "I experienced trembling",  // Anxiety
        "I felt that I was using a lot of nervous energy",  // Stress
        "I was worried about situations",  // Anxiety
        "I felt that I had nothing to look forward to",  // Depression
        "I found myself getting agitated",  // Stress
        "I found it difficult to relax",  // Anxiety
        "I felt down-hearted and blue",  // Depression
        "I was intolerant of anything",  // Stress
        "I felt I was close to panic",  // Anxiety
        "I was unable to become enthusiastic about anything",  // Depression
        "I felt I wasn’t worth much as a person",  // Depression
        "I felt that I was rather touchy",  // Stress
        "I was aware of the action of my heart",  // Anxiety
        "I felt scared without any good reason",  // Anxiety
        "I felt that life was meaningless"  // Depression
    )
    val options = listOf("Did not apply to me at all", "Applied to me to some degree", "Applied to me to a considerable degree", "Applied to me very much")

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val currentQuestion = questions.getOrElse(currentQuestionIndex) { "No more questions" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "In the past week, how much have the following applied to you?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentQuestion,
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        options.forEachIndexed { index, option ->
            Button(
                onClick = {
                    when (currentQuestionIndex) {
                        in 0..4 -> depressionScore += index // Depression-related questions
                        in 5..9 -> anxietyScore += index // Anxiety-related questions
                        else -> stressScore += index // Stress-related questions
                    }

                    totalScore += index
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex += 1
                    } else {
                        navController.navigate("dassResult/$depressionScore/$anxietyScore/$stressScore")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5FB1FF))
            ) {
                Text(text = option, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun DASSResultScreen(depressionScore: Int, anxietyScore: Int, stressScore: Int, userViewModel: UserViewModel, quizViewModel: QuizViewModel) {
    val user by userViewModel.currentUser.observeAsState()
    val userId = user?.userID ?: return

    // Calculate severities for each score
    val depressionSeverity = getDASSSeverity(depressionScore)
    val anxietySeverity = getDASSSeverity(anxietyScore)
    val stressSeverity = getDASSSeverity(stressScore)

    LaunchedEffect(userId) {
        // Save results to database
        quizViewModel.saveDASSQuizResultOnce(
            userId.toString(),
            depressionScore,
            anxietyScore,
            stressScore,
            depressionSeverity,
            anxietySeverity,
            stressSeverity
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Your DASS-21 scores are:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Depression: $depressionScore (Severity: $depressionSeverity)", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(text = "Anxiety: $anxietyScore (Severity: $anxietySeverity)", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(text = "Stress: $stressScore (Severity: $stressSeverity)", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (depressionScore >= 21 || anxietyScore >= 21 || stressScore >= 21) {
            Text(
                text = "It is recommended to talk to a mental health professional.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
        }
    }
}

fun getDASSSeverity(score: Int): String {
    return when (score) {
        in 0..9 -> "Normal"
        in 10..13 -> "Mild"
        in 14..20 -> "Moderate"
        in 21..27 -> "Severe"
        else -> "Extremely Severe"
    }
}
