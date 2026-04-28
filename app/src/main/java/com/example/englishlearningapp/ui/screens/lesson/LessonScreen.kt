package com.example.englishlearningapp.ui.screens.lesson

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.englishlearningapp.data.model.Lesson
import com.example.englishlearningapp.data.model.Topic
import com.example.englishlearningapp.ui.navigation.Routes
import com.example.englishlearningapp.ui.theme.Primary

enum class LessonStatus {
    COMPLETED, ACTIVE, LOCKED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    navController: NavController,
    topicId: String,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val currentTopic by viewModel.currentTopic.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lessonProgress by viewModel.lessonProgress.collectAsStateWithLifecycle()

    LaunchedEffect(topicId) {
        viewModel.loadData(topicId)
    }

    Scaffold(
        containerColor = Color(0xFFFAF8FF),
        topBar = {
            TopAppBar(
                title = { Text(currentTopic?.name ?: "Bài học", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    HeroSection(topic = currentTopic)
                }

                itemsIndexed(lessons) { index, lesson ->
                    val progressStatus = lessonProgress[lesson.lessonId]
                    
                    val status = when {
                        progressStatus == "completed" -> LessonStatus.COMPLETED
                        index == 0 || (index > 0 && lessonProgress[lessons[index - 1].lessonId] == "completed") -> LessonStatus.ACTIVE
                        else -> LessonStatus.LOCKED
                    }

                    LessonItem(
                        lesson = lesson,
                        status = status,
                        onClick = { 
                            if (status != LessonStatus.LOCKED) {
                                navController.navigate(Routes.vocabularyList(lesson.lessonId))
                            }
                        }
                    )
                }

                item {
                    StudyTipSection()
                }
            }
        }
    }
}

@Composable
fun HeroSection(topic: Topic?) {
    val topicName = topic?.name ?: "Đang tải..."
    
    val icon = when (topic?.iconType?.lowercase()) {
        "work" -> Icons.Default.Work
        "flight" -> Icons.Default.Flight
        "restaurant" -> Icons.Default.Restaurant
        "school" -> Icons.Default.School
        "nature" -> Icons.Default.Eco
        else -> Icons.Default.Explore
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = size.minDimension * 0.5f,
                        center = Offset(size.width * 0.95f, size.height * 0.05f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.minDimension * 0.3f,
                        center = Offset(size.width * 0.1f, size.height * 0.9f)
                    )
                }
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CHỦ ĐỀ",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = topicName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Surface(
                    modifier = Modifier.size(72.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp).size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LessonItem(lesson: Lesson, status: LessonStatus, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedOffset by animateDpAsState(targetValue = if (isPressed) 2.dp else 0.dp, label = "offset")
    val animatedElevation by animateDpAsState(targetValue = if (isPressed) 1.dp else 4.dp, label = "elevation")
    val animatedAlpha by animateFloatAsState(targetValue = if (status == LessonStatus.LOCKED) 0.7f else 1f, label = "alpha")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .alpha(animatedAlpha)
    ) {
        if (status != LessonStatus.LOCKED) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = 4.dp)
                    .background(
                        color = if (status == LessonStatus.ACTIVE) Primary.copy(alpha = 0.2f) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = animatedOffset)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = status != LessonStatus.LOCKED,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = if (status == LessonStatus.ACTIVE) BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer) else null,
            shadowElevation = animatedElevation
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = when (status) {
                                LessonStatus.COMPLETED -> Color(0xFFE8F5E9)
                                LessonStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                LessonStatus.LOCKED -> Color(0xFFF5F5F5)
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (status) {
                            LessonStatus.COMPLETED -> Icons.Default.CheckCircle
                            LessonStatus.ACTIVE -> Icons.Default.PlayArrow
                            LessonStatus.LOCKED -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        tint = when (status) {
                            LessonStatus.COMPLETED -> Color(0xFF4CAF50)
                            LessonStatus.ACTIVE -> Primary
                            LessonStatus.LOCKED -> Color.Gray
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (status == LessonStatus.LOCKED) Color.Gray else Color.Black
                    )
                    Text(
                        text = if (status == LessonStatus.COMPLETED) "Ôn tập bài học này" else "${lesson.totalWords} từ vựng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                if (status == LessonStatus.ACTIVE) {
                    PressableStartButton(onClick = onClick)
                } else if (status == LessonStatus.COMPLETED) {
                    Text(
                        "100%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }

        if (status == LessonStatus.ACTIVE) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 20.dp, y = (-10).dp),
                color = Primary,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    "TIẾP TỤC HỌC",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PressableStartButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedYOffset by animateDpAsState(targetValue = if (isPressed) 2.dp else 0.dp, label = "btnOffset")

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .offset(y = 4.dp)
                .size(width = 80.dp, height = 40.dp)
                .background(Color(0xFF1A45A0), RoundedCornerShape(12.dp))
        )
        
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .offset(y = animatedYOffset)
                .size(width = 80.dp, height = 40.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
        ) {
            Text("BẮT ĐẦU", fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

@Composable
fun StudyTipSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawRoundRect(
                    color = Color(0xFFFBC02D).copy(alpha = 0.4f),
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFBC02D),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Mẹo học tập",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Kiên trì là chìa khóa! Hãy cố gắng hoàn thành ít nhất một bài học mỗi ngày.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}
