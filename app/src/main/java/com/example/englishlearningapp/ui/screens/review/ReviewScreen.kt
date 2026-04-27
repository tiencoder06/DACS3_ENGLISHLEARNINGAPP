package com.example.englishlearningapp.ui.screens.review

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.ui.screens.quiz.QuizQuestionContent
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val questions by viewModel.questions
    val currentIndex by viewModel.currentIndex
    val userAnswer by viewModel.userAnswer
    val isAnswered by viewModel.isAnswered
    val isCorrect by viewModel.isCorrect
    val isFinished by viewModel.isFinished
    val correctCount by viewModel.correctCount
    val isLoading by viewModel.isLoading

    val context = LocalContext.current
    val tts = remember {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) textToSpeech?.language = Locale.US
        }
        textToSpeech
    }

    // Nếu màn hình được mở mà không có câu hỏi (từ ProgressScreen), hãy tải các câu hỏi từ khó
    LaunchedEffect(questions) {
        if (questions.isEmpty()) {
            viewModel.loadWeakWordsQuestions()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ÔN TẬP CÂU SAI", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1CB0F6))
            } else if (isFinished) {
                ReviewSummary(
                    total = questions.size,
                    correct = correctCount,
                    onBack = onBack
                )
            } else if (questions.isNotEmpty() && currentIndex < questions.size) {
                val currentQuestion = questions[currentIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress Bar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E5E5))
                        ) {
                            val progress = (currentIndex + 1).toFloat() / questions.size
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFA000), Color(0xFFFF6F00))))
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${currentIndex + 1}/${questions.size}", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    QuizQuestionContent(
                        question = currentQuestion,
                        userAnswer = userAnswer,
                        onAnswerChange = { viewModel.onAnswerChange(it) },
                        onPlayAudio = {
                            tts?.stop()
                            tts?.speak(currentQuestion.audioText ?: "", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Feedback Area
                    if (isAnswered) {
                        FeedbackCard(isCorrect = isCorrect, correctAnswer = currentQuestion.correctAnswer)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = {
                            if (!isAnswered) viewModel.checkAnswer()
                            else viewModel.nextQuestion()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAnswered) Color(0xFF1CB0F6) else if (isCorrect) Color(0xFF58CC02) else Color(0xFFFF4B4B)
                        ),
                        enabled = userAnswer.isNotBlank()
                    ) {
                        Text(
                            if (!isAnswered) "KIỂM TRA" else if (currentIndex < questions.size - 1) "TIẾP THEO" else "HOÀN THÀNH",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(64.dp), Color(0xFF58CC02))
                        Spacer(Modifier.height(16.dp))
                        Text("Bạn không có câu hỏi nào cần ôn tập!", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onBack) { Text("QUAY LẠI") }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(isCorrect: Boolean, correctAnswer: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCorrect) Color(0xFFDDF4FF).copy(alpha = 0.5f) else Color(0xFFFFEBEB),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isCorrect) Color(0xFF58CC02) else Color(0xFFFF4B4B))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isCorrect) Color(0xFF58CC02) else Color(0xFFFF4B4B),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isCorrect) "Chính xác!" else "Chưa đúng rồi",
                    fontWeight = FontWeight.Black,
                    color = if (isCorrect) Color(0xFF58CC02) else Color(0xFFFF4B4B)
                )
                if (!isCorrect) {
                    Text("Đáp án đúng: $correctAnswer", color = Color(0xFFFF4B4B))
                }
            }
        }
    }
}

@Composable
fun ReviewSummary(total: Int, correct: Int, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("HOÀN THÀNH ÔN TẬP", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Kết quả ôn tập", color = Color.Gray)
                Text("$correct / $total", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF1CB0F6))
                Text("câu đúng", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("QUAY LẠI", fontWeight = FontWeight.Black)
        }
    }
}
