package com.example.englishlearningapp.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Tự động làm mới khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadProgress()
    }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thống kê học tập", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1CB0F6))
            } else {
                progress?.let { data ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EvaluationCard(data.evaluation, data.suggestion)

                        // Các chỉ số chi tiết theo mockup
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatRow("Số lần luyện tập", "${data.totalQuizAttempts}", Icons.Default.History, Color(0xFF1CB0F6))
                            StatRow("Điểm trung bình", "${data.averageScore}%", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF58CC02))
                            StatRow("Điểm cao nhất", "${data.bestScore}", Icons.Default.EmojiEvents, Color(0xFFFFC107))
                            StatRow("Điểm lần cuối", "${data.lastScore}", Icons.Default.Star, Color(0xFFFF5722))
                            StatRow("Tổng câu trả lời đúng", "${data.totalCorrectAnswers}", Icons.Default.CheckCircle, Color(0xFF9C27B0))
                        }

                        if (data.mistakeCount > 0) {
                            Button(
                                onClick = onNavigateToMistakes,
                                modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA2B2B)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Warning, null)
                                Spacer(Modifier.width(12.dp))
                                Text("LÀM LẠI ${data.mistakeCount} CÂU SAI", fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có dữ liệu thống kê", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EvaluationCard(evaluation: String, suggestion: String) {
    val mainColor = when(evaluation) {
        "Xuất sắc" -> Color(0xFF1CB0F6)
        "Khá" -> Color(0xFFFBC02D)
        else -> Color(0xFFEA2B2B)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, mainColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Đánh giá của bạn", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(evaluation, fontSize = 28.sp, fontWeight = FontWeight.Black, color = mainColor)
            if (suggestion.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(suggestion, textAlign = TextAlign.Center, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(color.copy(0.1f), CircleShape).padding(8.dp)) {
                    Icon(icon, null, Modifier.size(24.dp), color)
                }
                Spacer(Modifier.width(16.dp))
                Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF4B4B4B), fontSize = 16.sp)
            }
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        }
    }
}
