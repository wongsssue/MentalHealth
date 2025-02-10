package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.QuestionnaireViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.flowOf

@Composable
fun QuestionnaireScreen(
    /*navController: NavController? = null, viewModel: QuestionnaireViewModel*/
    onNavigate: (String) -> Unit
) {
    //val questions by viewModel.questions.collectAsState()
    val currentIndex = remember { mutableStateOf(0) }
    val answers = remember { mutableStateOf(mutableMapOf<Int, String?>()) }
    val questions = listOf("I have difficulty falling asleep.", "Do you feel stressed?")
    Box {
        // Header
        Text(
            text = "SLEEP QUALITY TEST",
            fontSize = 30.sp,
            color = Color(0xFF2E3E64),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        )
        if (questions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 110.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(450.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Display Question Number
                        Text(
                            text = "Question ${currentIndex.value + 1}/ ${questions.size} : ",
                            style = MaterialTheme.typography.h5,
                            color = Color(0xFF2E3E64),
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 20.dp, top = 30.dp)
                        )
                        // Display Current Question
                        Text(
                            text = questions[currentIndex.value],
                            style = MaterialTheme.typography.h6,
                            color = Color(0xFF2E3E64),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Radio Group for Answers
                        RadioGroup(
                            options = listOf(
                                "Agree",
                                "Semi-Agree",
                                "Neutral",
                                "Semi-Disagree",
                                "Disagree"
                            ),
                            selectedOption = answers.value[currentIndex.value],
                            onOptionSelected = { selectedOption ->
                                answers.value[currentIndex.value] = selectedOption
                            }
                        )
                    }

                }

                Spacer(modifier = Modifier.height(30.dp))

                // Next or Submit Button
                Button(onClick = {
                    if (currentIndex.value < questions.size - 1) {
                        currentIndex.value++
                    } else {
                        // Submit Answers
                        /*
                        viewModel.submitAnswers(answers.value.values.toList())
                        val result = calculateResult(answers.value) // Example placeholder
                        navController?.navigate("result/$result")*/
                    }
                },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFAB9FFF)),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .height(50.dp)
                        .width(150.dp),
                    elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
                    ) {
                    Text(
                        text = if (currentIndex.value == questions.size - 1) "Submit" else "Next",
                        color = Color.White,
                        fontSize = 20.sp
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
fun RadioGroup(options: List<String>, selectedOption: String?, onOptionSelected: (String) -> Unit) {
    Column {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onOptionSelected(option) }
            ) {
                RadioButton(
                    selected = (selectedOption == option),
                    onClick = { onOptionSelected(option) }
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.body1,
                    color = Color(0xFF2E3E64),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewQuestionnaireScreen() {
    QuestionnaireScreen(onNavigate = {})
}

// Example function to calculate results based on answers (replace with your logic)
fun calculateResult(answers: Map<Int, String?>): String {
    // Example: Return "poor" if most answers are negative
    val negativeResponses = listOf("Disagree", "Semi-Disagree")
    val score = answers.values.count { it in negativeResponses }
    return when {
        score > answers.size / 2 -> "poor"
        else -> "good"
    }
}
