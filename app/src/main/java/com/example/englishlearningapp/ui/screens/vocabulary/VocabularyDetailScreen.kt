package com.example.englishlearningapp.ui.screens.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyDetailScreen(
    navController: NavController,
    lessonId: String,
    viewModel: VocabularyDetailViewModel = hiltViewModel()
) {
    val vocabularies by viewModel.vocabularies.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLearned by viewModel.isLearned.collectAsStateWithLifecycle()
    val favoriteStatus by viewModel.favoriteStatus.collectAsStateWithLifecycle()
    val difficultStatus by viewModel.difficultStatus.collectAsStateWithLifecycle()
    
    val pagerState = rememberPagerState { vocabularies.size }

    LaunchedEffect(lessonId) {
        viewModel.loadVocabularies(lessonId)
    }

    // Update learned status when page changes
    LaunchedEffect(pagerState.currentPage, vocabularies) {
        if (vocabularies.isNotEmpty()) {
            viewModel.checkIfVocabularyLearned(vocabularies[pagerState.currentPage].vocabId)
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAF8FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Vocabulary Detail", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (vocabularies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Indicator
                LinearProgressIndicator(
                    progress = { if (vocabularies.isNotEmpty()) (pagerState.currentPage + 1).toFloat() / vocabularies.size else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .height(6.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = Color(0xFFE0E0E0),
                    color = Color(0xFF004AC6)
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val vocabulary = vocabularies[page]
                    val isFavorite = favoriteStatus[vocabulary.vocabId] ?: false
                    val isDifficult = difficultStatus[vocabulary.vocabId] ?: false

                    VocabularyContent(
                        vocabulary = vocabulary,
                        isFavorite = isFavorite,
                        isDifficult = isDifficult,
                        isLearned = isLearned,
                        onSoundClick = { viewModel.playSound(vocabulary) },
                        onFavoriteToggle = { viewModel.toggleFavorite(vocabulary.vocabId) },
                        onDifficultToggle = { viewModel.toggleDifficult(vocabulary.vocabId) },
                        onLearnedClick = { viewModel.onMarkLearnedClick(vocabulary) }
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No vocabulary found.")
            }
        }
    }
}

@Composable
fun VocabularyContent(
    vocabulary: Vocabulary,
    isFavorite: Boolean,
    isDifficult: Boolean,
    isLearned: Boolean,
    onSoundClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDifficultToggle: () -> Unit,
    onLearnedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WordCard(vocabulary, onSoundClick)

        Spacer(modifier = Modifier.height(24.dp))

        DetailCard(vocabulary)

        Spacer(modifier = Modifier.height(32.dp))

        BottomControls(
            isFavorite = isFavorite,
            isDifficult = isDifficult,
            isLearned = isLearned,
            onFavoriteToggle = onFavoriteToggle,
            onDifficultToggle = onDifficultToggle,
            onLearnedClick = onLearnedClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WordCard(vocabulary: Vocabulary, onSoundClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0xFF004AC6).copy(alpha = 0.1f)
            )
            .background(Color.White, RoundedCornerShape(32.dp))
            .drawBehind {
                drawCircle(
                    color = Color(0xFF2563EB).copy(alpha = 0.06f),
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.9f, size.height * 0.1f)
                )
                drawCircle(
                    color = Color(0xFF6CF8BB).copy(alpha = 0.06f),
                    radius = size.width * 0.35f,
                    center = Offset(size.width * 0.1f, size.height * 0.9f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFEDEDF9),
                shape = CircleShape
            ) {
                Text(
                    text = vocabulary.partOfSpeech.uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = Color(0xFF004AC6),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = vocabulary.word,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF004AC6),
                textAlign = TextAlign.Center
            )

            Text(
                text = vocabulary.pronunciation,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(Color(0xFF003896), CircleShape)
                    .padding(bottom = 4.dp)
                    .background(Color(0xFF004AC6), CircleShape)
                    .clickable { onSoundClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Pronunciation",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun DetailCard(vocabulary: Vocabulary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF004AC6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Meaning",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF004AC6),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = vocabulary.meaning,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2D3436),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Example",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF004AC6),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val example = vocabulary.getDisplayExample()
            if (example.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawLine(
                                color = Color(0xFF004AC6),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 4.dp.toPx()
                            )
                        }
                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = example,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 26.sp
                        ),
                        color = Color(0xFF636E72)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomControls(
    isFavorite: Boolean,
    isDifficult: Boolean,
    isLearned: Boolean,
    onFavoriteToggle: () -> Unit,
    onDifficultToggle: () -> Unit,
    onLearnedClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorite Button
        Surface(
            onClick = onFavoriteToggle,
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFF5252) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Difficult Button
        Surface(
            onClick = onDifficultToggle,
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isDifficult) Icons.Filled.Flag else Icons.Outlined.Flag,
                    contentDescription = "Difficult",
                    tint = if (isDifficult) Color(0xFFFFA000) else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Learned Button
        Button(
            onClick = onLearnedClick,
            enabled = !isLearned,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLearned) Color(0xFF4CAF50) else Primary,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF4CAF50),
                disabledContentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLearned) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Learned",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        text = "I Learned This",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
