package com.example.englishlearningapp.ui.screens.quiz

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val timeLeft by viewModel.timeLeft
    val isTimeUp by viewModel.isTimeUp

    var showExitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    BackHandler(enabled = quizResult == null) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát bài kiểm tra?", fontWeight = FontWeight.Bold) },
            text = { Text("Kết quả hiện tại sẽ không được lưu. Bạn có chắc chắn muốn thoát?") },
            confirmButton = {
                TextButton(onClick = onBack) { Text("THOÁT", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("TIẾP TỤC THI") }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    LaunchedEffect(quizResult) {
        quizResult?.let {
            onNavigateToResult(it.score, it.correctCount, it.totalQuestions)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    QuizProgressHeader(
                        currentIndex = currentIndex,
                        totalQuestions = questions.size
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.LightGray)
                    }
                },
                actions = {
                    QuizTimer(timeLeft = timeLeft)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1CB0F6))
            } else if (questions.isNotEmpty() && currentIndex < questions.size) {
                val currentQuestion = questions[currentIndex]
                val currentAnswer = userAnswers[currentIndex] ?: ""
                val scrollState = rememberScrollState()

                LaunchedEffect(currentIndex) {
                    scrollState.animateScrollTo(0)
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        QuizQuestionContent(
                            question = currentQuestion,
                            userAnswer = currentAnswer,
                            onAnswerChange = { viewModel.onAnswer(it) },
                            onPlayAudio = {
                                tts.language = Locale.US
                                tts.speak(currentQuestion.correctAnswer, TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) viewModel.nextQuestion()
                            else viewModel.submitQuiz()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = currentAnswer.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1CB0F6),
                            disabledContainerColor = Color(0xFFE5E5E5)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (currentIndex < questions.size - 1) "TIẾP THEO" else "NỘP BÀI",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (currentAnswer.isNotBlank()) Color.White else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizProgressHeader(currentIndex: Int, totalQuestions: Int) {
    val progress by animateFloatAsState(
        targetValue = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f,
        label = "QuizProgressAnimation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
    ) {
        Box(modifier = Modifier.weight(1f).height(12.dp)) {
            Box(
                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFE5E5E5))
            )
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).clip(CircleShape).background(Color(0xFF58CC02))
            )
            Box(
                modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(progress).padding(horizontal = 8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))
            )
        }
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
fun QuizTimer(timeLeft: Int) {
    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timerColor = if (timeLeft < 60) Color(0xFFEA2B2B) else Color(0xFF1CB0F6)
    
    Surface(
        color = timerColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = timerColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                color = timerColor,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
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
            color = Color(0xFFF0F7FF),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when(question.type) {
                    QuestionType.MULTIPLE_CHOICE -> "CHỌN ĐÁP ÁN ĐÚNG"
                    QuestionType.FILL_BLANK -> "ĐIỀN TỪ CÒN THIẾU"
                    QuestionType.AUDIO -> "NGHE VÀ CHỌN KẾT QUẢ"
                    else -> "CÂU HỎI"
                },
                color = Color(0xFF1CB0F6),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (question.type == QuestionType.AUDIO) {
            Surface(
                onClick = onPlayAudio,
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color(0xFF1CB0F6),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Nhấn để nghe", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(
                text = question.question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF4B4B4B),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (question.type == QuestionType.MULTIPLE_CHOICE || question.type == QuestionType.AUDIO) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                question.options?.forEach { option ->
                    val isSelected = userAnswer == option

                    Surface(
                        onClick = { onAnswerChange(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFFDDF4FF) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 2.dp,
                            color = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)
                        ),
                        shadowElevation = if (isSelected) 0.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, Modifier.size(16.dp), Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isSelected) Color(0xFF1CB0F6) else Color(0xFF4B4B4B)
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = userAnswer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Gõ câu trả lời tại đây...", color = Color.LightGray) },
                shape = RoundedCornerShape(20.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1CB0F6),
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                    focusedContainerColor = Color(0xFFF0F7FF)
                )
            )
        }
    }
}
