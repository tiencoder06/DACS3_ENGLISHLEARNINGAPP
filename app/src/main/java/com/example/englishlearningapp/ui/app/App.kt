package com.example.englishlearningapp.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.englishlearningapp.ui.components.navigation.AppBottomBar
import com.example.englishlearningapp.ui.navigation.AppNavGraph

@Composable
fun EnglishLearningApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            AppBottomBar(navController = navController)
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
