package com.example.englishlearningapp.ui.screens.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.ui.screens.topic.TopicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPartSelectionScreen(
    lessonId: String,
    onBack: () -> Unit,
    onPartSelected: (Int) -> Unit,
    topicViewModel: TopicViewModel = hiltViewModel()
) {
    val topics by topicViewModel.topics.collectAsState()
    
    // Tìm tên chủ đề dựa trên lessonId (ở đây giả định lessonId khớp với topicId trong context này)
    val topicName = if (lessonId == "general") {
        "Tổng hợp"
    } else {
        topics.find { it.id == lessonId }?.name ?: "Chủ đề"
    }

    val parts = listOf(
        QuizPart(1, "Part 1: $topicName", "Kiểm tra kiến thức cơ bản"),
        QuizPart(2, "Part 2: Nghe câu hỏi và trả lời", "Phản xạ nghe hiểu"),
        QuizPart(3, "Part 3: Đoạn hội thoại ngắn", "Hiểu ngữ cảnh giao tiếp"),
        QuizPart(4, "Part 4: Bài nói ngắn", "Kỹ năng nghe chi tiết")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn Part kiểm tra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Bài kiểm tra: $topicName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Vui lòng chọn phần bạn muốn kiểm tra:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp),
                color = Color.Gray
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(parts) { part ->
                    PartItem(part = part, onClick = { onPartSelected(part.id) })
                }
            }
        }
    }
}

data class QuizPart(val id: Int, val title: String, val description: String)

@Composable
fun PartItem(part: QuizPart, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = part.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = part.description,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}
