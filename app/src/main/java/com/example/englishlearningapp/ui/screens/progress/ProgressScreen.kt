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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.data.model.Progress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProgress()
    }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TIẾN TRÌNH CỦA BẠN", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF7F7F7))) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1CB0F6))
            } else {
                progress?.let { data ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Accuracy Header
                        AccuracyHeader(data.accuracy, data.correctAnswers, data.totalAnswers)

                        // Progress Badge (Đang tiến bộ)
                        if (data.accuracy >= 50) {
                            StatusBadge("Đang tiến bộ! 🚀", Color(0xFF58CC02))
                        }

                        // Weak Words Card
                        WeakWordsCard(
                            count = data.weakWordsCount,
                            onAction = onNavigateToMistakes
                        )

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SmallStatBox("Bài kiểm tra", "${data.totalQuizAttempts}", Icons.Default.Quiz, Color(0xFF1CB0F6), Modifier.weight(1f))
                            SmallStatBox("Từ đã thuộc", "${data.totalVocabulariesLearned}", Icons.Default.MenuBook, Color(0xFF58CC02), Modifier.weight(1f))
                        }

                        // Suggestion Card
                        SuggestionBox(data.suggestion)
                        
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccuracyHeader(accuracy: Int, correct: Int, total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ĐỘ CHÍNH XÁC", fontWeight = FontWeight.Black, color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                CircularProgressIndicator(
                    progress = { if (total > 0) accuracy.toFloat() / 100f else 0f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF58CC02),
                    strokeWidth = 12.dp,
                    trackColor = Color(0xFFE5E5E5),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text("$accuracy%", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Bạn đã làm đúng $correct/$total câu hỏi",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B4B4B)
            )
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeakWordsCard(count: Int, onAction: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning, 
                    contentDescription = null, 
                    tint = if (count > 0) Color(0xFFEA2B2B) else Color.Gray
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Bạn đang yếu $count từ",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (count > 0) Color(0xFFEA2B2B) else Color(0xFF4B4B4B)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Bạn nên ôn lại các từ đã sai gần đây để ghi nhớ tốt hơn.",
                color = Color.DarkGray,
                fontSize = 14.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onAction,
                enabled = count > 0,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1CB0F6),
                    disabledContainerColor = Color(0xFFE5E5E5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Autorenew, 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (count > 0) "ÔN LẠI LỖI SAI" else "Bạn chưa có từ cần ôn", 
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun SmallStatBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B4B4B))
            Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SuggestionBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF0F7FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1CB0F6).copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, null, tint = Color(0xFF1CB0F6))
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 14.sp, color = Color(0xFF1CB0F6), fontWeight = FontWeight.Medium)
        }
    }
}
