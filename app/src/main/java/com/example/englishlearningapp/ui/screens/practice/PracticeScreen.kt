package com.example.englishlearningapp.ui.screens.practice

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.data.model.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val questions by viewModel.questions
    val currentIndex by viewModel.currentIndex
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val showFeedback by viewModel.showFeedback
    val isCorrect by viewModel.isCorrect
    val isFinished by viewModel.isFinished
    val userAnswer by viewModel.userAnswer

    var showExitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    BackHandler(enabled = !isFinished) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Dừng luyện tập?", fontWeight = FontWeight.Bold) },
            text = { Text("Tiến trình luyện tập sẽ không được lưu. Bạn có chắc muốn thoát?") },
            confirmButton = {
                TextButton(onClick = onBack) { Text("THOÁT", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("TIẾP TỤC") }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (isFinished) {
        PracticeResultScreen(
            correctCount = viewModel.correctCount.value,
            totalCount = questions.size,
            wrongCount = viewModel.wrongQuestionsCount,
            onRetry = { viewModel.retry() },
            onRetryMistakes = { viewModel.retryMistakes() },
            onBack = onBack
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        PracticeProgressHeader(
                            currentIndex = currentIndex,
                            totalQuestions = questions.size
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showExitDialog = true }) { 
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray) 
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
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error ?: "Unknown error", textAlign = TextAlign.Center, color = Color.Gray)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.retry() }, shape = RoundedCornerShape(12.dp)) { Text("THỬ LẠI") }
                    }
                } else if (questions.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Không tìm thấy dữ liệu bài học.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) { Text("QUAY LẠI") }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
                            AnimatedContent(
                                targetState = currentIndex,
                                transitionSpec = {
                                    slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                                }, label = "QuestionAnimation"
                            ) { index ->
                                questions.getOrNull(index)?.let {
                                    PracticeContent(
                                        question = it,
                                        viewModel = viewModel,
                                        tts = tts
                                    )
                                }
                            }
                        }

                        // Bottom Action Bar
                        Column(modifier = Modifier.padding(24.dp)) {
                            val currentQuestion = questions.getOrNull(currentIndex)
                            val canCheck = userAnswer.isNotBlank() || currentQuestion?.type == PracticeType.MATCHING

                            if (!showFeedback) {
                                Button(
                                    onClick = { 
                                        if (currentQuestion?.type == PracticeType.MATCHING) {
                                            // Matching logic is handled within the component usually, 
                                            // but if we want a "CONTINUE" button for matching:
                                            viewModel.nextQuestion()
                                        } else {
                                            viewModel.checkAnswer()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = canCheck,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1CB0F6),
                                        disabledContainerColor = Color(0xFFE5E5E5)
                                    )
                                ) {
                                    Text(
                                        if (currentQuestion?.type == PracticeType.MATCHING) "TIẾP TỤC" else "KIỂM TRA", 
                                        fontWeight = FontWeight.Black, 
                                        fontSize = 18.sp
                                    )
                                }
                            } else {
                                // Feedback handles its own button
                                Spacer(modifier = Modifier.height(56.dp))
                            }
                        }
                    }

                    // Feedback Overlay
                    var feedbackSnapshot by remember { mutableStateOf<Triple<Boolean, String, String>?>(null) }
                    LaunchedEffect(showFeedback) {
                        if (showFeedback) {
                            val q = questions.getOrNull(currentIndex)
                            feedbackSnapshot = Triple(isCorrect, q?.correctAnswer ?: "", q?.vocabulary?.exampleSentence ?: "")
                        }
                    }

                    AnimatedVisibility(
                        visible = showFeedback,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        feedbackSnapshot?.let { (snapshotIsCorrect, snapshotAnswer, snapshotExample) ->
                            FeedbackOverlay(
                                isCorrect = snapshotIsCorrect,
                                correctAnswer = snapshotAnswer,
                                explanation = snapshotExample,
                                onNext = {
                                    viewModel.nextQuestion()
                                    feedbackSnapshot = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PracticeProgressHeader(currentIndex: Int, totalQuestions: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalQuestions > 0) (currentIndex + 1).toFloat() / totalQuestions else 0f,
        label = "ProgressAnimation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
    ) {
        Box(modifier = Modifier.weight(1f).height(12.dp)) {
            // Track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFE5E5E5))
            )
            // Progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(Color(0xFF58CC02))
            )
            // Shine effect on progress
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.3f)
                    .fillMaxWidth(animatedProgress)
                    .padding(horizontal = 8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "Câu ${currentIndex + 1}/$totalQuestions",
            fontWeight = FontWeight.Black,
            color = Color(0xFF4B4B4B),
            fontSize = 14.sp
        )
    }
}

@Composable
fun PracticeContent(question: PracticeQuestion, viewModel: PracticeViewModel, tts: TextToSpeech) {
    val userAnswer by viewModel.userAnswer
    val showFeedback by viewModel.showFeedback
    val isCorrect by viewModel.isCorrect
    
    val selectedLeft by viewModel.selectedLeft
    val selectedRight by viewModel.selectedRight
    val matchedPairs = viewModel.matchedPairs
    val wrongLeft by viewModel.wrongLeft
    val wrongRight by viewModel.wrongRight

    PracticeQuestionContent(
        question = question,
        userAnswer = userAnswer,
        onAnswer = { viewModel.onSelectAnswer(it) },
        showFeedback = showFeedback,
        isCorrect = isCorrect,
        tts = tts,
        selectedLeft = selectedLeft,
        selectedRight = selectedRight,
        matchedPairs = matchedPairs,
        wrongLeft = wrongLeft,
        wrongRight = wrongRight,
        onMatchingSelect = { item, isLeft -> viewModel.onMatchingSelect(item, isLeft) }
    )
}

@Composable
fun PracticeQuestionContent(
    question: PracticeQuestion,
    userAnswer: String,
    onAnswer: (String) -> Unit,
    showFeedback: Boolean,
    isCorrect: Boolean,
    tts: TextToSpeech? = null,
    selectedLeft: String? = null,
    selectedRight: String? = null,
    matchedPairs: List<String> = emptyList(),
    wrongLeft: String? = null,
    wrongRight: String? = null,
    onMatchingSelect: ((String, Boolean) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        if (question.type != PracticeType.MATCHING) {
            Surface(
                color = Color(0xFFF0F7FF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = when(question.type) {
                        PracticeType.EN_TO_VI -> "DỊCH TỪ NÀY"
                        PracticeType.VI_TO_EN -> "CHỌN TỪ TIẾNG ANH ĐÚNG"
                        PracticeType.AUDIO_TO_VI -> "NGHE VÀ CHỌN NGHĨA"
                        PracticeType.FILL_BLANK -> "NHẬP TỪ CÒN THIẾU"
                        else -> "LUYỆN TẬP"
                    },
                    color = Color(0xFF1CB0F6),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        when (question.type) {
            PracticeType.EN_TO_VI -> {
                WordDisplay(question.vocabulary.word) {
                    tts?.language = Locale.US
                    tts?.speak(question.vocabulary.word, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                OptionList(question.options, userAnswer, showFeedback, question.correctAnswer, isCorrect) { onAnswer(it) }
            }
            PracticeType.VI_TO_EN -> {
                WordDisplay(question.vocabulary.meaning)
                OptionList(question.options, userAnswer, showFeedback, question.correctAnswer, isCorrect) { onAnswer(it) }
            }
            PracticeType.AUDIO_TO_VI -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        onClick = {
                            tts?.language = Locale.US
                            tts?.speak(question.vocabulary.word, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color(0xFF1CB0F6),
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color.White, modifier = Modifier.size(60.dp))
                        }
                    }
                }
                OptionList(question.options, userAnswer, showFeedback, question.correctAnswer, isCorrect) { onAnswer(it) }
            }
            PracticeType.FILL_BLANK -> {
                WordDisplay(question.vocabulary.meaning)
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { if (!showFeedback) onAnswer(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    placeholder = { Text("Gõ tại đây...") },
                    enabled = !showFeedback,
                    isError = showFeedback && !isCorrect,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (showFeedback) (if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)) else Color(0xFF1CB0F6),
                        unfocusedBorderColor = if (showFeedback) (if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)) else Color(0xFFE5E5E5),
                        focusedContainerColor = Color(0xFFF0F7FF)
                    )
                )
            }
            PracticeType.MATCHING -> {
                if (onMatchingSelect != null) {
                    MatchingGameContent(
                        pairs = question.matchingPairs,
                        selectedLeft = selectedLeft,
                        selectedRight = selectedRight,
                        matchedPairs = matchedPairs,
                        wrongLeft = wrongLeft,
                        wrongRight = wrongRight,
                        onMatchingSelect = onMatchingSelect
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun FeedbackOverlay(isCorrect: Boolean, correctAnswer: String, explanation: String, onNext: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isCorrect) Color(0xFFD7FFB8) else Color(0xFFFFDFE0),
        shadowElevation = 20.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (isCorrect) "Chính xác!" else "Chưa đúng rồi",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)
                    )
                    if (!isCorrect && correctAnswer.isNotBlank()) {
                        Text("Đáp án đúng: $correctAnswer", fontWeight = FontWeight.Bold, color = Color(0xFFEA2B2B))
                    }
                }
            }

            if (explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ví dụ: $explanation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B))
            ) {
                Text("TIẾP TỤC", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun MatchingGameContent(
    pairs: List<Pair<String, String>>,
    selectedLeft: String?,
    selectedRight: String?,
    matchedPairs: List<String>,
    wrongLeft: String?,
    wrongRight: String?,
    onMatchingSelect: (String, Boolean) -> Unit
) {
    val leftItems = remember(pairs) { pairs.map { it.first }.shuffled() }
    val rightItems = remember(pairs) { pairs.map { it.second }.shuffled() }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Nối các cặp từ tương ứng",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = Color(0xFF4B4B4B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        )

        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                leftItems.forEach { item ->
                    MatchingCard(
                        text = item,
                        isSelected = selectedLeft == item,
                        isMatched = matchedPairs.contains(item),
                        isWrong = wrongLeft == item,
                        modifier = Modifier.weight(1f),
                        onClick = { onMatchingSelect(item, true) }
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rightItems.forEach { item ->
                    MatchingCard(
                        text = item,
                        isSelected = selectedRight == item,
                        isMatched = matchedPairs.contains(item),
                        isWrong = wrongRight == item,
                        modifier = Modifier.weight(1f),
                        onClick = { onMatchingSelect(item, false) }
                    )
                }
            }
        }
    }
}

@Composable
fun MatchingCard(
    text: String, 
    isSelected: Boolean, 
    isMatched: Boolean, 
    isWrong: Boolean, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = when {
        isMatched -> Color(0xFF58CC02)
        isWrong -> Color(0xFFEA2B2B)
        isSelected -> Color(0xFF1CB0F6)
        else -> Color(0xFFE5E5E5)
    }

    val backgroundColor = when {
        isMatched -> Color(0xFFD7FFB8)
        isWrong -> Color(0xFFFFDFE0)
        isSelected -> Color(0xFFDDF4FF)
        else -> Color.White
    }

    val contentColor = when {
        isMatched -> Color(0xFF58CC02)
        isWrong -> Color(0xFFEA2B2B)
        isSelected -> Color(0xFF1CB0F6)
        else -> Color(0xFF4B4B4B)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isMatched) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        shadowElevation = if (isSelected || isMatched) 0.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WordDisplay(text: String, onPlay: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF7F7F7),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onPlay != null) {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier.background(Color(0xFF1CB0F6), CircleShape).size(56.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(20.dp))
            }
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4B4B4B)
            )
        }
    }
}

@Composable
fun OptionList(
    options: List<String>,
    selected: String,
    showFeedback: Boolean,
    correctAnswer: String,
    isCorrect: Boolean,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            val isActuallyCorrect = option == correctAnswer

            val backgroundColor = when {
                showFeedback && isActuallyCorrect -> Color(0xFFD7FFB8)
                showFeedback && isSelected && !isCorrect -> Color(0xFFFFDFE0)
                isSelected -> Color(0xFFDDF4FF)
                else -> Color.White
            }

            val borderColor = when {
                showFeedback && isActuallyCorrect -> Color(0xFF58CC02)
                showFeedback && isSelected && !isCorrect -> Color(0xFFEA2B2B)
                isSelected -> Color(0xFF1CB0F6)
                else -> Color(0xFFE5E5E5)
            }

            Surface(
                onClick = { if (!showFeedback) onSelect(option) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = backgroundColor,
                border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
                shadowElevation = if (isSelected || showFeedback) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected || (showFeedback && isActuallyCorrect)) borderColor else Color(0xFFE5E5E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showFeedback && isActuallyCorrect) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp), Color.White)
                        } else if (showFeedback && isSelected && !isCorrect) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp), Color.White)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = option,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected || (showFeedback && isActuallyCorrect)) 
                            (if (showFeedback && isSelected && !isCorrect) Color(0xFFEA2B2B) else Color(0xFF1CB0F6)) 
                            else Color(0xFF4B4B4B)
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeResultScreen(
    correctCount: Int,
    totalCount: Int,
    wrongCount: Int,
    onRetry: () -> Unit,
    onRetryMistakes: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Hoàn thành bài luyện!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F1F1F))
        Text("Luyện tập giúp bạn nhớ lâu hơn.", color = Color.Gray, fontSize = 16.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bạn đã đúng $correctCount/$totalCount câu",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF58CC02)
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            val accuracy = if (totalCount > 0) (correctCount * 100) / totalCount else 0
            ResultStatCard("Chính xác", "$accuracy%", Color(0xFF58CC02))
            ResultStatCard("Đã luyện", "$totalCount câu", Color(0xFF1CB0F6))
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (wrongCount > 0) {
            Button(
                onClick = onRetryMistakes,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA2B2B))
            ) {
                Text("ÔN LẠI CÂU SAI ($wrongCount)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3A67))
        ) {
            Text("LUYỆN TẬP LẠI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("QUAY LẠI BÀI HỌC", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ResultStatCard(label: String, value: String, color: Color) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
