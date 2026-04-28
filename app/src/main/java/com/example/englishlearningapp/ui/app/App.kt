package com.example.englishlearningapp.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.englishlearningapp.ui.components.navigation.AppBottomBar
import com.example.englishlearningapp.ui.navigation.AppNavGraph
import com.example.englishlearningapp.ui.screens.practice.PracticeViewModel
import com.example.englishlearningapp.utils.TextToSpeechHelper

@Composable
fun EnglishLearningApp(
    ttsHelper: TextToSpeechHelper
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            ttsHelper = ttsHelper
        )
    }
}
