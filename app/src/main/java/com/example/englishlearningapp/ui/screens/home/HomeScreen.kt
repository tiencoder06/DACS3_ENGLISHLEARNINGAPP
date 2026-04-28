package com.example.englishlearningapp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGoToTopic: () -> Unit,
    onGoToProgress: () -> Unit,
    onGoToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Screen")

        Button(onClick = onGoToTopic, modifier = Modifier.padding(top = 16.dp)) {
            Text("Go to Topics")
        }

        Button(onClick = onGoToProgress, modifier = Modifier.padding(top = 8.dp)) {
            Text("Go to Progress")
        }

        Button(onClick = onGoToProfile, modifier = Modifier.padding(top = 8.dp)) {
            Text("Go to Profile")
        }
    }
}