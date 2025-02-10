package com.example.mentalhealthemotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mentalhealthemotion.ui.theme.MentalHealthEmotionTheme
import com.example.mentalhealthemotion.ui.theme.MentalHeathApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MentalHealthEmotionTheme {
                MentalHeathApp()
            }
        }
    }
}

