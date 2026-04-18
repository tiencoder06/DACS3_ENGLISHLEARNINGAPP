package com.example.englishlearningapp.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.englishlearningapp.ui.navigation.AppNavGraph

@Composable
fun EnglishLearningApp() {
    Scaffold { innerPadding ->
        AppNavGraph(modifier = Modifier.padding(innerPadding))
    }
}
