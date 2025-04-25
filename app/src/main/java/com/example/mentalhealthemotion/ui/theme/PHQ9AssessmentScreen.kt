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
fun PHQ9AssessmentScreen(navController: NavController) {
    var totalScore by remember { mutableStateOf(0) }
    val questions = listOf(
        "Little interest or pleasure in doing things",
        "Feeling down, depressed or hopeless",
        "Trouble falling asleep, staying asleep, or sleeping too much",
        "Feeling tired or having little energy",
        "Poor appetite or overeating",
        "Feeling bad about yourself - or that you’re a failure or have let yourself or your family down",
        "Trouble concentrating on things, such as reading the newspaper or watching television",
        "Moving or speaking so slowly that other people could have noticed. Or, the opposite - being so fidgety or restless that you have been moving around a lot more than usual",
        "Thoughts that you would be better off dead or of hurting yourself in some way"
    )
    val options = listOf("Not at all", "Several days", "More than half the days", "Nearly every day")

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
            text = "Over the last 2 weeks, how often have you been bothered by the following problem?",
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
                    totalScore += index // Add score based on answer
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex += 1
                    } else {
                        navController.navigate("result/$totalScore")
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
fun ResultScreen(score: Int, userViewModel: UserViewModel, quizViewModel: QuizViewModel) {
    val user by userViewModel.currentUser.observeAsState()
    val userId = user?.userID ?: return

    LaunchedEffect(userId) {
        quizViewModel.loadQuiz(userId.toString()) {}
        quizViewModel.saveQuizResultOnce(userId.toString(), score, getSeverity(score))
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
                Text(text = "Your PHQ-9 score is: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Severity: ${getSeverity(score)}", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Blue)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (score >= 10) {
            Text(
                text = "Please consult a healthcare provider for further assistance.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
        }
    }
}

fun getSeverity(score: Int): String {
    return when (score) {
        in 1..4 -> "Minimal depression"
        in 5..9 -> "Mild depression"
        in 10..14 -> "Moderate depression"
        in 15..19 -> "Moderately severe depression"
        in 20..27 -> "Severe depression"
        else -> "No depression detected"
    }
}