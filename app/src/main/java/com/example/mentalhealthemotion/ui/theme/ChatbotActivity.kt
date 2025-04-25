package com.example.mentalhealthemotion.ui.theme

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.R
import java.io.BufferedReader
import java.io.InputStreamReader

class ChatbotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userInput = intent.getStringExtra("user_input") ?: "How are you feeling today?"

        setContent {
            ChatbotScreen(userInput)
        }
    }
}

@Composable
fun ChatbotScreen(userInput: String) {
    var response by remember { mutableStateOf("Waiting for response...") }

    LaunchedEffect(userInput) {
        response = startChatbot(userInput)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "User Input: $userInput", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = response, fontSize = 16.sp)
    }
}

fun startChatbot(userInput: String): String {
    return try {
        val pythonScript = "chatbot.py"
        val command = arrayOf("python3", pythonScript, userInput)

        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        reader.readText().trim()
    } catch (e: Exception) {
        "Error running chatbot: ${e.message}"
    }
}
