package com.example.englishlearningapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onGoToTopic: () -> Unit,
    onGoToProgress: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToPractice: () -> Unit,
    onGoToQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "English Learning",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeCard(
                title = "Luyện tập",
                subtitle = "Học từ mới",
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f),
                onClick = onGoToPractice,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            HomeCard(
                title = "Kiểm tra",
                subtitle = "Đánh giá",
                icon = Icons.Default.Quiz,
                modifier = Modifier.weight(1f),
                onClick = onGoToQuiz,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HomeCard(
            title = "Chủ đề bài học",
            subtitle = "Khám phá kiến thức mới",
            icon = Icons.Default.Topic,
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToTopic,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeCard(
                title = "Tiến độ",
                subtitle = "Thành tích",
                icon = Icons.Default.History,
                modifier = Modifier.weight(1f),
                onClick = onGoToProgress
            )
            HomeCard(
                title = "Cá nhân",
                subtitle = "Tài khoản",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                onClick = onGoToProfile
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
