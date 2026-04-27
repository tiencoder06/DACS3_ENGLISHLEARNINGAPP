package com.example.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.englishlearningapp.data.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    onRetry: () -> Unit,
    onBackHome: () -> Unit,
    onReview: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val result by viewModel.quizResult

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("KẾT QUẢ BÀI THI", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1CB0F6))
            }
        } else {
            result?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Result Header with Icon
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            color = Color(0xFFFFC107).copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {}
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (data.score >= 80) "Tuyệt vời!" else if (data.score >= 50) "Khá tốt!" else "Cố gắng lên!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        "Bạn đã hoàn thành bài kiểm tra.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ResultStatCard(
                            label = "ĐIỂM SỐ",
                            value = "${data.score}",
                            subValue = "/100",
                            color = Color(0xFF1CB0F6),
                            modifier = Modifier.weight(1f)
                        )
                        ResultStatCard(
                            label = "CHÍNH XÁC",
                            value = "${data.correctCount}",
                            subValue = "/${data.totalQuestions}",
                            color = Color(0xFF58CC02),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Wrong questions section
                    if (data.wrongQuestions.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Xem lại lỗi sai (${data.wrongQuestions.size})",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4B4B4B),
                                fontSize = 16.sp
                            )
                            TextButton(onClick = onReview) {
                                Text("ÔN TẬP", fontWeight = FontWeight.Black, color = Color(0xFF1CB0F6))
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(data.wrongQuestions) { (question, userAnswer) ->
                                WrongQuestionCard(question, userAnswer)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFD7FFB8).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF58CC02), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Hoàn hảo! Không có lỗi sai nào.",
                                    color = Color(0xFF58CC02),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Bottom Buttons
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
                        ) {
                            Text("THỬ LẠI", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        OutlinedButton(
                            onClick = onBackHome,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5))
                        ) {
                            Text("VỀ TRANG CHỦ", fontWeight = FontWeight.Black, color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultStatCard(label: String, value: String, subValue: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.2f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Black)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = color)
                Text(subValue, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

@Composable
fun WrongQuestionCard(question: Question, userAnswer: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F7F7),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.question,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B4B4B),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, null, tint = Color(0xFFEA2B2B), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Của bạn: $userAnswer", color = Color(0xFFEA2B2B), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF58CC02), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Đáp án: ${question.correctAnswer}", color = Color(0xFF58CC02), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
