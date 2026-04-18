package com.example.englishlearningapp.ui.screens.topic

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TopicScreen(
    onTopicClick: () -> Unit
) {
    Button(onClick = onTopicClick) {
        Text("Go to Lesson Screen")
    }
}