package com.example.englishlearningapp.ui.screens.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.data.model.PracticeType
import com.example.englishlearningapp.ui.screens.practice.PracticeQuestionContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewPracticeScreen(
    onBack: () -> Unit,
    viewModel: ReviewPracticeViewModel = hiltViewModel()
) {
    val questions by viewModel.questions
    val currentIndex by viewModel.currentIndex
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val isFinished by viewModel.isFinished
    val showFeedback by viewModel.showFeedback
    val isCorrect by viewModel.isCorrect
    val userAnswer by viewModel.userAnswer
    val correctCount by viewModel.correctCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ReviewProgressHeader(
                        currentIndex = currentIndex,
                        totalQuestions = questions.size
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1CB0F6))
            } else if (error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error!!, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))) { 
                        Text("QUAY LẠI", fontWeight = FontWeight.Bold) 
                    }
                }
            } else if (isFinished) {
                ReviewPracticeSummary(
                    total = questions.size,
                    correct = correctCount,
                    onBack = onBack,
                    onRetry = { viewModel.retry() }
                )
            } else if (questions.isNotEmpty()) {
                val currentQuestion = questions[currentIndex]
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PracticeQuestionContent(
                        question = currentQuestion,
                        userAnswer = userAnswer,
                        onAnswer = { viewModel.onAnswer(it) },
                        showFeedback = showFeedback,
                        isCorrect = isCorrect
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (showFeedback) {
                        ReviewFeedbackCard(
                            isCorrect = isCorrect,
                            correctAnswer = currentQuestion.correctAnswer,
                            example = currentQuestion.vocabulary.exampleSentence
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            if (!showFeedback) {
                                viewModel.checkAnswer()
                            } else {
                                viewModel.nextQuestion()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showFeedback) Color(0xFF1CB0F6) else if (isCorrect) Color(0xFF58CC02) else Color(0xFFFF4B4B)
                        ),
                        enabled = userAnswer.isNotBlank() || showFeedback
                    ) {
                        Text(
                            if (!showFeedback) "KIỂM TRA" else "TIẾP THEO",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewProgressHeader(currentIndex: Int, totalQuestions: Int) {
    val progress by animateFloatAsState(
        targetValue = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f,
        label = "ReviewProgressAnimation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(CircleShape),
            color = Color(0xFF58CC02),
            trackColor = Color(0xFFE5E5E5)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${currentIndex + 1}/$totalQuestions",
            fontWeight = FontWeight.Black,
            color = Color(0xFF4B4B4B),
            fontSize = 14.sp
        )
    }
}

@Composable
fun ReviewFeedbackCard(isCorrect: Boolean, correctAnswer: String, example: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) Color(0xFFD7FFB8).copy(alpha = 0.5f) else Color(0xFFFFDFE0).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isCorrect) Color(0xFF58CC02).copy(alpha = 0.3f) else Color(0xFFEA2B2B).copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isCorrect) "Chính xác!" else "Chưa đúng rồi",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)
                )
            }
            
            if (!isCorrect) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Đáp án đúng: $correctAnswer", 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEA2B2B)
                )
            }
            
            if (example.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Ví dụ:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                Text(example, fontSize = 15.sp, color = Color(0xFF4B4B4B))
            }
        }
    }
}

@Composable
fun ReviewPracticeSummary(total: Int, correct: Int, onBack: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF58CC02),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("HOÀN THÀNH ÔN TẬP", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F1F1F))
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Bạn đã trả lời đúng", color = Color.Gray, fontSize = 16.sp)
        Text("$correct/$total", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color(0xFF1CB0F6))
        Text("câu hỏi", fontWeight = FontWeight.Bold, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58CC02))
        ) {
            Text("LÀM LẠI", fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5))
        ) {
            Text("QUAY LẠI", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Gray)
        }
    }
}
