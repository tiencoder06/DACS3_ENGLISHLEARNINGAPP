package com.example.englishlearningapp.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.ui.theme.Primary

@Composable
fun VocabularyFlashcard(
    vocabulary: Vocabulary,
    onSoundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Tag cho Part of Speech
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = CircleShape,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = vocabulary.partOfSpeech.lowercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Word (Size 40, ExtraBold)
            Text(
                text = vocabulary.word,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A237E), // Indigo-ish
                textAlign = TextAlign.Center
            )

            // 3. Pronunciation
            Text(
                text = "/${vocabulary.pronunciation}/",
                style = MaterialTheme.typography.titleLarge,
                color = Primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // 4. Floating-style Audio Button (Giữa thẻ)
            FilledIconButton(
                onClick = onSoundClick,
                modifier = Modifier
                    .size(72.dp)
                    .shadow(8.dp, CircleShape),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Pronunciation",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // 5. Meaning
            Text(
                text = vocabulary.meaning,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF2D3436)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Example Sentence (Border Start Primary, Italic)
            val example = vocabulary.exampleSentence.ifEmpty { vocabulary.example }
            if (example.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                        .padding(start = 12.dp) // Space for the border
                ) {
                    // Custom border start effect
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(Primary, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    )
                    
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .border(
                                width = 0.dp, 
                                color = Color.Transparent
                            ) // Just to make sure padding is relative to box
                    ) {
                        Text(
                            text = example,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = FontStyle.Italic,
                                lineHeight = 24.sp
                            ),
                            color = Color(0xFF636E72)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
