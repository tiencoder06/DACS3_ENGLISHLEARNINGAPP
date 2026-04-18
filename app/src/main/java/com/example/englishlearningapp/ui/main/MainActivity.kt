package com.example.englishlearningapp.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.englishlearningapp.ui.app.EnglishLearningApp
import com.example.englishlearningapp.ui.theme.EnglishLearningAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnglishLearningAppTheme {
                EnglishLearningApp()
            }
        }
    }
}
