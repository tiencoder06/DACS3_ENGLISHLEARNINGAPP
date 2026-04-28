package com.example.englishlearningapp.ui.screens.topic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.englishlearningapp.data.model.Topic
import com.example.englishlearningapp.ui.navigation.Routes
import com.example.englishlearningapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(
    navController: NavController,
    viewModel: TopicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFFFAF8FF),
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "LingoPro",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = "Thông báo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Khám phá Chủ đề",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Chọn một danh mục để bắt đầu hành trình học tập",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            when (val state = uiState) {
                is TopicUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is TopicUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.topics.let { topics ->
                            itemsIndexed(topics) { index, topic ->
                                TopicCardItem(
                                    topic = topic,
                                    index = index,
                                    onClick = { navController.navigate(Routes.lessonList(topic.topicId)) }
                                )
                            }
                        }
                    }
                }
                is TopicUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Có lỗi xảy ra: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun TopicCardItem(
    topic: Topic,
    index: Int,
    onClick: () -> Unit
) {
    val glowColors = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFFF59E0B), // Orange
        Color(0xFF8B5CF6), // Purple
        Color(0xFF10B981)  // Green
    )
    val glowColor = glowColors[index % glowColors.size]
    
    val icon = when (topic.iconType.lowercase()) {
        "work" -> Icons.Default.Work
        "flight" -> Icons.Default.Flight
        "restaurant" -> Icons.Default.Restaurant
        "school" -> Icons.Default.School
        "nature" -> Icons.Default.Eco
        else -> Icons.Default.Explore
    }

    val level = (index % 5) + 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.15f),
                            radius = size.width * 0.6f
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.85f, size.height * 0.15f)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = glowColor.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = glowColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Cấp độ $level",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.DarkGray
                        )
                    }
                }

                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = topic.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = topic.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
