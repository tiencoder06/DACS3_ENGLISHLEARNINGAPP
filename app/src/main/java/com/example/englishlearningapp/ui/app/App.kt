package com.example.englishlearningapp.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.englishlearningapp.ui.components.FloatingAIButton
import com.example.englishlearningapp.ui.components.navigation.AppBottomBar
import com.example.englishlearningapp.ui.navigation.AppNavGraph
import com.example.englishlearningapp.ui.navigation.Routes
import com.example.englishlearningapp.ui.screens.ai.AIChatBottomSheet
import com.example.englishlearningapp.utils.TextToSpeechHelper

@Composable
fun EnglishLearningApp(
    ttsHelper: TextToSpeechHelper
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    var showAIChat by remember { mutableStateOf(false) }

    // Logic hiển thị nút AI: Hiện ở các màn hình học tập chính, ẩn ở Auth/Placement/Quiz/Splash
    val showAIButton = when (currentRoute) {
        Routes.HOME,
        Routes.TOPIC,
        Routes.LESSON,
        Routes.VOCABULARY_LIST,
        Routes.VOCABULARY_DETAIL,
        Routes.PRACTICE,
        Routes.RESULT,
        Routes.REVIEW,
        Routes.PROGRESS,
        Routes.PROFILE,
        Routes.EDIT_PROFILE -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        },
        floatingActionButton = {
            FloatingAIButton(
                onClick = { showAIChat = true },
                visible = showAIButton
            )
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            ttsHelper = ttsHelper
        )
        
        if (showAIChat) {
            AIChatBottomSheet(
                onDismiss = { showAIChat = false }
            )
        }
    }
}
