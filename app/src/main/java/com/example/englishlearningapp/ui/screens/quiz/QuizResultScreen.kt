package com.example.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

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
                title = { Text("KẾT QUẢ", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF7F8FA))
            )
        }
    ) { padding ->
        result?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F8FA))
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Trophy Icon
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Hoàn thành!", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F1F1F))
                Text("Bạn đã học rất tốt hôm nay.", color = Color.Gray, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(32.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    ResultCard("Chính xác", "${data.score}%", Color(0xFF58CC02))
                    ResultCard("Số câu", "${data.correctCount}/${data.totalQuestions}", Color(0xFF1CB0F6))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Wrong questions list header
                if (data.wrongQuestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Câu trả lời chưa đúng:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        TextButton(onClick = onReview) {
                            Text("ÔN TẬP LẠI", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1CB0F6))
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(data.wrongQuestions) { (question, userAnswer) ->
                        WrongQuestionItem(question, userAnswer)
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3A67))
                    ) {
                        Text("THỬ LẠI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    OutlinedButton(
                        onClick = onBackHome,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5))
                    ) {
                        Text("VỀ TRANG CHỦ", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(label: String, value: String, color: Color) {
    Surface(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.8f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun WrongQuestionItem(question: com.example.englishlearningapp.data.model.Question, userAnswer: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(question.question, fontWeight = FontWeight.Bold, color = Color(0xFF4B4B4B))
            Text("Bạn chọn: $userAnswer", color = Color.Red, fontSize = 14.sp)
            Text("Đáp án: ${question.correctAnswer}", color = Color(0xFF58CC02), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
