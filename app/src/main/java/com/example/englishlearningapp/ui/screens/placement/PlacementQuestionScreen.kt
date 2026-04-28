package com.example.englishlearningapp.ui.screens.placement

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.englishlearningapp.utils.TextToSpeechHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementQuestionScreen(
    uiState: PlacementUiState,
    ttsHelper: TextToSpeechHelper,
    onAnswerSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
    val isLastQuestion = uiState.currentQuestionIndex == uiState.questions.size - 1
    
    var showExitDialog by remember { mutableStateOf(false) }

    // Handle System Back Button
    BackHandler {
        showExitDialog = true
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Thoát bài kiểm tra?") },
            text = { Text("Tiến trình hiện tại sẽ không được lưu nếu bạn thoát.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) {
                    Text("Thoát", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tiếp tục làm bài")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kiểm tra đầu vào") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Câu ${uiState.currentQuestionIndex + 1} / ${uiState.questions.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.questions.isNotEmpty()) "${((uiState.currentQuestionIndex + 1) * 100) / uiState.questions.size}%" else "0%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val progress = if (uiState.questions.isNotEmpty()) {
                        (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = StrokeCap.Round
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // Question Content
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentQuestion.section == "listening" || currentQuestion.audioText.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        ttsHelper.speak(currentQuestion.audioText.ifEmpty { currentQuestion.questionText })
                                    },
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Nghe")
                                }
                            }
                            
                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Options
                    currentQuestion.options.forEach { option ->
                        val isSelected = uiState.selectedAnswer == option
                        
                        OutlinedCard(
                            onClick = { onAnswerSelected(option) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onAnswerSelected(option) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Quay lại")
                            }
                        }

                        Button(
                            onClick = {
                                if (isLastQuestion) onSubmitClick() else onNextClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            enabled = uiState.selectedAnswer.isNotEmpty() && !uiState.isSaving,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (isLastQuestion) "Hoàn thành" else "Tiếp theo")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Không có câu hỏi.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
