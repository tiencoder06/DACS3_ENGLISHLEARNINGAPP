package com.example.englishlearningapp.ui.screens.vocabulary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.ui.navigation.Routes
import com.example.englishlearningapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyListScreen(
    navController: NavController,
    lessonId: String,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val vocabularies by viewModel.vocabularies.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(lessonId) {
        viewModel.loadVocabularies(lessonId)
    }

    val filteredVocabs = remember(searchQuery, vocabularies) {
        vocabularies.filter { 
            it.word.contains(searchQuery, ignoreCase = true) || 
            it.meaning.contains(searchQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        topBar = {
            VocabularyTopBar(navController)
        },
        bottomBar = {
            if (!isLoading && vocabularies.isNotEmpty()) {
                VocabularyActionButtons(
                    onPracticeClick = { navController.navigate(Routes.practice(lessonId)) },
                    onQuizClick = { navController.navigate(Routes.quiz(lessonId)) }
                )
            }
        },
        containerColor = Color(0xFFFAF8FF)
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VocabularyHeader()
                }

                item {
                    VocabularySearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }

                items(filteredVocabs) { vocab ->
                    VocabCard(
                        vocab = vocab,
                        onSoundClick = { viewModel.playVocabularySound(vocab) },
                        onItemClick = { navController.navigate(Routes.vocabularyDetail(lessonId)) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for bottom buttons
                }
            }
        }
    }
}

@Composable
fun VocabularyActionButtons(
    onPracticeClick: () -> Unit,
    onQuizClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPracticeClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("LUYỆN TẬP", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onQuizClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("LÀM KIỂM TRA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "LingoPro",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Primary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun VocabularyHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Danh sách Từ vựng",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularySearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp)),
        placeholder = { Text("Tìm kiếm từ vựng...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Primary.copy(alpha = 0.5f),
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
fun VocabCard(
    vocab: Vocabulary,
    onSoundClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onItemClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vocab.word,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = CircleShape,
                        onClick = onSoundClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Phát âm",
                            tint = Primary,
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }
                }
                Text(
                    text = "/${vocab.word.lowercase()}/",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}
