package com.example.englishlearningapp.ui.screens.quiz

import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.englishlearningapp.data.model.Question
import com.example.englishlearningapp.data.model.QuestionType
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBack: () -> Unit,
    onNavigateToResult: (Int, Int, Int) -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val questions by viewModel.questions
    val currentIndex by viewModel.currentIndex
    val userAnswers by viewModel.userAnswers
    val quizResult by viewModel.quizResult
    val isLoading by viewModel.isLoading

    val context = LocalContext.current
    val tts = remember {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setPitch(1.0f)
                textToSpeech?.setSpeechRate(0.9f)
            }
        }
        textToSpeech
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(quizResult) {
        quizResult?.let {
            onNavigateToResult(it.score, it.correctCount, it.totalQuestions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Luyện tập", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (questions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hiện chưa có câu hỏi nào cho bài học này.", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                        Text("QUAY LẠI")
                    }
                }
            } else if (currentIndex < questions.size) {
                val currentQuestion = questions[currentIndex]
                val currentAnswer = userAnswers[currentIndex] ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Thanh tiến trình
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E5E5))
                        ) {
                            val progress = (currentIndex + 1).toFloat() / questions.size
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF58CC02), Color(0xFF23AC38))))
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${currentIndex + 1}/${questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    QuizQuestionContent(
                        question = currentQuestion,
                        userAnswer = currentAnswer,
                        onAnswerChange = { viewModel.onAnswer(it) },
                        onPlayAudio = {
                            val audioText = currentQuestion.audioText
                            val textToSpeak = when {
                                !audioText.isNullOrBlank() -> audioText
                                currentQuestion.question.isNotBlank() -> currentQuestion.question
                                else -> currentQuestion.correctAnswer
                            }
                            
                            if (textToSpeak.any { it in 'à'..'ỹ' || it in 'À'..'Ỹ' }) {
                                tts?.language = Locale("vi", "VN")
                            } else {
                                tts?.language = Locale.US
                            }

                            tts?.stop()
                            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) viewModel.nextQuestion()
                            else viewModel.submitQuiz()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1CB0F6),
                            disabledContainerColor = Color(0xFFE5E5E5),
                            contentColor = Color.White,
                            disabledContentColor = Color.Gray
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            disabledElevation = 0.dp
                        ),
                        enabled = currentAnswer.isNotBlank()
                    ) {
                        Text(
                            text = if (currentIndex < questions.size - 1) "TIẾP THEO" else "HOÀN THÀNH",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizQuestionContent(
    question: Question,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    onPlayAudio: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF0F7FF),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD0E4FF))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (question.type == QuestionType.AUDIO) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, Color(0xFF1CB0F6), CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF1CB0F6), modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nghe âm thanh và trả lời", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color(0xFF4B4B4B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (question.type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.AUDIO -> {
                if (!question.options.isNullOrEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        question.options?.forEach { option ->
                            val isSelected = userAnswer == option
                            val borderColor by animateColorAsState(if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5))
                            val bgColor by animateColorAsState(if (isSelected) Color(0xFFDDF4FF) else Color.White)

                            Surface(
                                onClick = { onAnswerChange(option) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = bgColor,
                                border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF1CB0F6) else Color(0xFF4B4B4B)
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = onAnswerChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập câu trả lời của bạn...") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1CB0F6),
                            unfocusedBorderColor = Color(0xFFE5E5E5)
                        ),
                        singleLine = true
                    )
                }
            }
            QuestionType.FILL_BLANK -> {
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = onAnswerChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập câu trả lời của bạn...") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1CB0F6),
                        unfocusedBorderColor = Color(0xFFE5E5E5)
                    ),
                    singleLine = true
                )
            }
            QuestionType.MATCHING -> {
                Text(
                    text = "Loại câu hỏi này chưa được hỗ trợ trong bài kiểm tra.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
