package com.example.mentalhealthemotion.ui.theme

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.PSQIQuestion
import com.example.mentalhealthemotion.Data.PSQIVIewModel
import com.example.mentalhealthemotion.Data.QuestionType
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R

@Composable
fun QuestionnaireScreen(
    pqsiViewModel: PSQIVIewModel,
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit,
    ResultScreen: () -> Unit
) {
    val currentIndex = remember { mutableStateOf(0) }
    val user by userViewModel.currentUser.observeAsState()
    val questions by pqsiViewModel.questions.observeAsState(emptyList())
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        currentIndex.value = 0  // Reset question index
        pqsiViewModel.clearResponses() // Ensure responses are cleared
    }

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
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        // Row for Back Button and Question Number
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentIndex.value > 0) {
                                        currentIndex.value--
                                    }
                                },
                                enabled = currentIndex.value > 0
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.back),
                                    contentDescription = "Back",
                                    tint = if (currentIndex.value > 0) Color(0xFF2E3E64) else Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Question ${currentIndex.value + 1} / ${questions.size} : ",
                                style = MaterialTheme.typography.h5,
                                color = Color(0xFF2E3E64),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Get current question
                        val question = questions[currentIndex.value]

                        // Display Current Question
                        Text(
                            text = question.question,
                            fontSize = 16.sp,
                            color = Color(0xFF2E3E64),
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Justify,
                            lineHeight = 25.sp,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display question type
                        when (question.type) {
                            is QuestionType.Objective -> {
                                ObjectiveQuestionItem(
                                    question = question,
                                    selectedOption = pqsiViewModel.responses.find { it.questionId == question.id }?.answer as? String ?: "",
                                    onAnswerSelected = { answer ->
                                        pqsiViewModel.updateResponse(question.id, answer)
                                    }
                                )
                            }

                            is QuestionType.Subjective -> {
                                SubjectiveQuestionItem(
                                    question = question,
                                    text = pqsiViewModel.responses.find { it.questionId == question.id }?.answer as? String ?: "",
                                    onAnswerEntered = { answer ->
                                        pqsiViewModel.updateResponse(question.id, answer)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Next or Submit Button
                Button(
                    onClick = {
                        val currentQuestion = questions[currentIndex.value]
                        val currentResponse = pqsiViewModel.responses.find { it.questionId == currentQuestion.id }

                        val answer = currentResponse?.answer?.toString() // Ensure it's a String

                        val isValid = when (currentQuestion.type) {
                            is QuestionType.Objective -> {
                                answer?.let { pqsiViewModel.getValidationError(it, currentQuestion.id) } ?: false
                            }
                            is QuestionType.Subjective -> {
                                answer?.let { pqsiViewModel.getValidationError(it, currentQuestion.id) } ?: false
                            }
                            else -> false
                        }

                        if (isValid) {
                            // Move to next question or submit
                            if (currentIndex.value < questions.size - 1) {
                                currentIndex.value++
                            } else {
                                user?.userID?.let { userId ->
                                    pqsiViewModel.submitResponses(userId, pqsiViewModel.responses,
                                        onSuccess = {
                                        ResultScreen()
                                    })
                                }
                            }
                        } else {
                            // Show validation error message
                            val errorMessage = pqsiViewModel.inputError.value ?: "This field cannot be empty" // Handle nullable String safely
                            if (errorMessage.isNotEmpty()) {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
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
fun ObjectiveQuestionItem(question: PSQIQuestion, selectedOption: String, onAnswerSelected: (String) -> Unit) {
    var selectedOptionState by remember { mutableStateOf(selectedOption) }

    LaunchedEffect(selectedOption) {  // Ensure pre-filled answer when navigating back
        selectedOptionState = selectedOption
    }

    Column(modifier = Modifier.padding(8.dp)) {
        question.type.let { type ->
            if (type is QuestionType.Objective) {
                type.options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = (option == selectedOptionState),
                            onClick = {
                                selectedOptionState = option
                                onAnswerSelected(option)
                            }
                        )
                    ) {
                        RadioButton(selected = (option == selectedOptionState), onClick = null)
                        Text(text = option, modifier = Modifier.padding(start = 8.dp), color = Color(0xFF2E3E64))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun SubjectiveQuestionItem(question: PSQIQuestion, text: String, onAnswerEntered: (String) -> Unit) {
    var textState by remember { mutableStateOf(text) }

    LaunchedEffect(text) { // Ensure pre-filled answer when navigating back
        textState = text
    }
    Column(modifier = Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = textState,
            onValueChange = {
                textState = it
                onAnswerEntered(it)
            },
            label = { Text("Your Answer") }
        )
    }
}


