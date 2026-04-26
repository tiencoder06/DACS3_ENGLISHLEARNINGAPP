package com.example.englishlearningapp.ui.screens.practice

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.data.model.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val questions by viewModel.questions
    val currentIndex by viewModel.currentIndex
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val isAnswered by viewModel.isAnswered
    val isCorrect by viewModel.isCorrect
    val isFinished by viewModel.isFinished
    val userAnswer by viewModel.userAnswer

    val context = LocalContext.current
    val tts = remember { TextToSpeech(context) { } }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

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
                        Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp)) {
                            LinearProgressIndicator(
                                progress = { (currentIndex + 1).toFloat() / (if (questions.isEmpty()) 1 else questions.size).toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Center),
                                color = Color(0xFF58CC02),
                                trackColor = Color(0xFFE5E5E5)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray) }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        Button(onClick = { viewModel.retry() }) { Text("THỬ LẠI") }
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
                        Button(onClick = onBack) { Text("QUAY LẠI") }
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

                        Column(modifier = Modifier.padding(24.dp)) {
                            val currentQuestion = questions.getOrNull(currentIndex)
                            val isMatching = currentQuestion?.type == PracticeType.MATCHING
                            val canCheck = if (isMatching) false else userAnswer.isNotBlank()

                            Button(
                                onClick = { viewModel.checkAnswer() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = canCheck && !isAnswered,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
                            ) {
                                Text("KIỂM TRA", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }

                    var feedbackSnapshot by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
                    LaunchedEffect(isAnswered) {
                        if (isAnswered) {
                            feedbackSnapshot = isCorrect to (questions.getOrNull(currentIndex)?.correctAnswer ?: "")
                        }
                    }

                    AnimatedVisibility(
                        visible = isAnswered,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        feedbackSnapshot?.let { (snapshotIsCorrect, snapshotAnswer) ->
                            FeedbackOverlay(
                                isCorrect = snapshotIsCorrect,
                                correctAnswer = snapshotAnswer,
                                onNext = { viewModel.nextQuestion() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackOverlay(isCorrect: Boolean, correctAnswer: String, onNext: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isCorrect) Color(0xFFD7FFB8) else Color(0xFFFFDFE0),
        shadowElevation = 20.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 8.dp)) {
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
fun WordDisplay(text: String, onPlay: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onPlay != null) {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier.background(Color(0xFF1CB0F6), CircleShape).size(44.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
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
fun OptionList(options: List<String>, selected: String, isAnswered: Boolean, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            val color = if (isSelected) Color(0xFFDDF4FF) else Color.White
            val borderColor = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)

            Surface(
                onClick = { if (!isAnswered) onSelect(option) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = color,
                border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF1CB0F6) else Color(0xFF4B4B4B)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeContent(question: PracticeQuestion, viewModel: PracticeViewModel, tts: TextToSpeech) {
    val userAnswer by viewModel.userAnswer
    val isAnswered by viewModel.isAnswered

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        when (question.type) {
            PracticeType.EN_TO_VI -> {
                Text("Chọn nghĩa đúng:", color = Color.Gray, fontWeight = FontWeight.Bold)
                WordDisplay(question.vocabulary.word) {
                    tts.language = Locale.US
                    tts.speak(question.vocabulary.word, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                OptionList(question.options, userAnswer, isAnswered) { viewModel.onSelectAnswer(it) }
            }
            PracticeType.VI_TO_EN -> {
                Text("Dịch sang tiếng Anh:", color = Color.Gray, fontWeight = FontWeight.Bold)
                WordDisplay(question.vocabulary.meaning)
                OptionList(question.options, userAnswer, isAnswered) { viewModel.onSelectAnswer(it) }
            }
            PracticeType.AUDIO_TO_VI -> {
                Text("Nghe và chọn đáp án:", color = Color.Gray, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            tts.language = Locale.US
                            tts.speak(question.vocabulary.word, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        modifier = Modifier.size(100.dp).background(Color(0xFF1CB0F6), CircleShape).shadow(4.dp, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
                OptionList(question.options, userAnswer, isAnswered) { viewModel.onSelectAnswer(it) }
            }
            PracticeType.FILL_BLANK -> {
                Text("Nhập từ tiếng Anh:", color = Color.Gray, fontWeight = FontWeight.Bold)
                WordDisplay(question.vocabulary.meaning)
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { if (!isAnswered) viewModel.onSelectAnswer(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Gõ tại đây...") },
                    enabled = !isAnswered
                )
            }
            PracticeType.MATCHING -> {
                Text("Nối các cặp tương ứng:", color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                MatchingGame(
                    pairs = question.matchingPairs,
                    selectedLeft = viewModel.selectedLeft.value,
                    selectedRight = viewModel.selectedRight.value,
                    wrongLeft = viewModel.wrongLeft.value,
                    wrongRight = viewModel.wrongRight.value,
                    matchedPairs = viewModel.matchedPairs,
                    onSelect = { item, isLeft -> viewModel.onMatchingSelect(item, isLeft) }
                )
            }
            PracticeType.SENTENCE_REORDER -> {
                Text("Sắp xếp các từ thành câu đúng:", color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(question.vocabulary.meaning, modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyLarge)

                // Hiển thị các từ đã chọn
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5)),
                    color = Color.White
                ) {
                    FlowRow(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.reorderSelectedWords.forEach { word ->
                            WordChip(word = word, onClick = { viewModel.onReorderWordClick(word, true) })
                        }
                    }
                }

                // Hiển thị các từ để chọn
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.shuffledWords.forEach { word ->
                        val isUsed = viewModel.reorderSelectedWords.contains(word)
                        WordChip(word = word, isEnabled = !isUsed, onClick = { viewModel.onReorderWordClick(word, false) })
                    }
                }
            }
        }
    }
}

@Composable
fun WordChip(word: String, isEnabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        onClick = { if(isEnabled) onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isEnabled) Color.White else Color(0xFFE5E5E5),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (isEnabled) Color(0xFFE5E5E5) else Color.Transparent),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = word,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) Color(0xFF4B4B4B) else Color.Transparent
        )
    }
}

@Composable
fun MatchingGame(
    pairs: List<Pair<String, String>>,
    selectedLeft: String?,
    selectedRight: String?,
    wrongLeft: String?,
    wrongRight: String?,
    matchedPairs: List<String>,
    onSelect: (String, Boolean) -> Unit
) {
    val leftItems = remember(pairs) { pairs.map { it.first }.shuffled() }
    val rightItems = remember(pairs) { pairs.map { it.second }.shuffled() }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            leftItems.forEach { item ->
                val isMatched = matchedPairs.contains(item)
                val isSelected = selectedLeft == item
                val isWrong = wrongLeft == item
                MatchingItem(item, isSelected, isMatched, isWrong) { onSelect(item, true) }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rightItems.forEach { item ->
                val isMatched = matchedPairs.contains(item)
                val isSelected = selectedRight == item
                val isWrong = wrongRight == item
                MatchingItem(item, isSelected, isMatched, isWrong) { onSelect(item, false) }
            }
        }
    }
}

@Composable
fun MatchingItem(text: String, isSelected: Boolean, isMatched: Boolean, isWrong: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isWrong -> Color(0xFFFFDFE0)
        isMatched -> Color(0xFFD7FFB8)
        isSelected -> Color(0xFFDDF4FF)
        else -> Color.White
    }

    val borderColor = when {
        isWrong -> Color(0xFFEA2B2B)
        isMatched -> Color(0xFF58CC02)
        isSelected -> Color(0xFF1CB0F6)
        else -> Color(0xFFE5E5E5)
    }

    val contentAlpha = if (isMatched) 0.5f else 1f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(enabled = !isMatched && !isWrong) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = (if (isWrong) Color(0xFFEA2B2B) else Color.Black).copy(alpha = contentAlpha)
            )
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
        // Trophy Icon
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Hoàn thành!", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F1F1F))
        Text("Bạn đã học rất tốt hôm nay.", color = Color.Gray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(40.dp))
        
        // Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            val accuracy = if (totalCount > 0) (correctCount * 100) / totalCount else 0
            ResultStatCard("Chính xác", "$accuracy%", Color(0xFF58CC02))
            ResultStatCard("Số câu", "$correctCount/$totalCount", Color(0xFF1CB0F6))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons
        if (wrongCount > 0) {
            Button(
                onClick = onRetryMistakes,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA2B2B))
            ) {
                Text("LÀM LẠI CÂU SAI ($wrongCount)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3A67)) // Dark blue like image
        ) {
            Text("LUYỆN TẬP LẠI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("QUAY LẠI", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
