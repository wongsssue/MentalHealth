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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ResultScreen(
    navController: NavController? = null,
    result: String,
    onNavigate: (String) -> Unit
) {
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
                    .height(400.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Result:",
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 50.dp, bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Your sleeping quality is $result.",
                        style = MaterialTheme.typography.h5,
                        lineHeight = 40.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 20.dp, bottom = 15.dp)
                    )
                    if (result.equals("poor", ignoreCase = true)) {
                        Text(
                            text = "You might need to look for ways to improve it.",
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Navigation Button
            Button(
                onClick = { onNavigate("home") }, // Replace navController
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

@Preview(showBackground = true)
@Composable
fun PreviewResultScreen() {
    ResultScreen(result = "Poor", onNavigate = {})
}
