package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.PSQIVIewModel

@Composable
fun ResultScreen(
    pqsiViewModel: PSQIVIewModel,
    onNavigate: (String) -> Unit,
    backHome:() -> Unit
) {

    val result by pqsiViewModel.result.observeAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "SLEEP QUALITY TEST",
            fontSize = 30.sp,
            color = Color(0xFF2E3E64),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card for displaying result
            Card(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
                    .height(350.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                result?.let{ pqsiResult ->
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append("Your Score : \n\n")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 35.sp, color = Color(0xFF2E3E64))) {
                                        append("${pqsiResult.score}")
                                    }
                                },
                                fontSize = 25.sp,
                                textAlign = TextAlign.Center, // Centers text inside Text component
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Sleep Quality : \n\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium,  fontSize = 20.sp, color = Color(0xFF2E3E64))) {
                                    append(pqsiResult.feedback)
                                }
                            },
                            fontSize = 25.sp,
                            lineHeight = 30.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 20.dp, bottom = 15.dp, start = 16.dp, end = 16.dp)
                        )


                    }

                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            // Navigation Button
            Button(
                onClick = { backHome() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFAB9FFF)),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .height(50.dp)
                    .width(150.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 10.dp)
            ) {
                Text(text = "Home", color = Color.White, fontSize = 20.sp)
            }
        }

        // Bottom Navigation Bar
        BottomNavigationBar(
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

