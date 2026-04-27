package com.example.englishlearningapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoToTopic: (String) -> Unit,
    onGoToProgress: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToPractice: () -> Unit,
    onGoToQuiz: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1CB0F6).copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF1CB0F6), modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("LingoPro", fontWeight = FontWeight.Black, fontSize = 22.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onGoToProgress) { 
                        Icon(Icons.Default.BarChart, "Stats", tint = Color(0xFF4B4B4B)) 
                    }
                    IconButton(onClick = onGoToProfile) { 
                        Icon(Icons.Default.AccountCircle, "Profile", tint = Color(0xFF4B4B4B)) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF7F7F7))) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1CB0F6))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Welcome
                    Text(
                        "Chào bạn quay trở lại! 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4B4B4B)
                    )

                    // A. Tiếp tục học
                    ContinueLearningCard(onContinue = { onGoToTopic("general") })

                    // B. Ôn lại lỗi sai
                    val weakCount = progress?.weakWordsCount ?: 0
                    if (weakCount > 0) {
                        ReviewMistakesCard(
                            count = weakCount,
                            onReview = { /* Navigate to review handled by navigation if needed, or simple onGoToPractice with flag */ 
                                // For now we assume a dedicated review screen exists
                                onGoToPractice() // Should be REVIEW_PRACTICE route but usually practice handles it
                            }
                        )
                    }

                    // D. Thống kê nhanh
                    QuickStatsSection(
                        accuracy = progress?.accuracy ?: 0,
                        wordsLearned = progress?.totalVocabulariesLearned ?: 0
                    )

                    // C. Luyện nhanh
                    QuickActionSection(onGoToPractice, onGoToQuiz)
                }
            }
        }
    }
}

@Composable
fun ContinueLearningCard(onContinue: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        onClick = onContinue
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF58CC02)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("TIẾP TỤC HỌC", fontWeight = FontWeight.Black, color = Color(0xFF58CC02), fontSize = 13.sp)
                Text("Bài học gần nhất", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4B4B4B))
                Text("Hoàn thành mục tiêu hôm nay nào!", color = Color.Gray, fontSize = 14.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ReviewMistakesCard(count: Int, onReview: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFDFE0),
        onClick = onReview
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFEA2B2B))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("ÔN LẠI LỖI SAI", fontWeight = FontWeight.Black, color = Color(0xFFEA2B2B), fontSize = 13.sp)
                Text("Bạn đang yếu $count từ", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF4B4B4B))
            }
            Button(
                onClick = onReview,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA2B2B)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("ÔN NGAY", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuickStatsSection(accuracy: Int, wordsLearned: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        HomeStatCard(
            label = "Chính xác",
            value = "$accuracy%",
            icon = Icons.Default.GpsFixed,
            color = Color(0xFF1CB0F6),
            modifier = Modifier.weight(1f)
        )
        HomeStatCard(
            label = "Đã học",
            value = "$wordsLearned từ",
            icon = Icons.Default.MenuBook,
            color = Color(0xFF58CC02),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HomeStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B))
            Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickActionSection(onPractice: () -> Unit, onQuiz: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("LUYỆN TẬP NHANH", fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B), fontSize = 15.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(
                title = "Luyện tập",
                subtitle = "Practice",
                icon = Icons.Default.Edit,
                color = Color(0xFF1CB0F6),
                modifier = Modifier.weight(1f),
                onClick = onPractice
            )
            ActionCard(
                title = "Kiểm tra",
                subtitle = "Quiz",
                icon = Icons.Default.Timer,
                color = Color(0xFF7851A9),
                modifier = Modifier.weight(1f),
                onClick = onQuiz
            )
        }
    }
}

@Composable
fun ActionCard(title: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = color,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}
