package com.example.englishlearningapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishlearningapp.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoToTopic: () -> Unit,
    onGoToProgress: () -> Unit,
    onGoToProfile: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFFAF8FF),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LingoPro",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Primary
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onGoToProfile) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Trang cá nhân", tint = Primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Welcome Header
            Text(
                text = "Chào mừng trở lại!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = "Hôm nay bạn muốn học gì?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Action Card (Topic)
            MainActionCard(
                title = "Khám phá Chủ đề",
                subtitle = "Bắt đầu bài học mới ngay hôm nay",
                icon = Icons.Default.MenuBook,
                color = Primary,
                onClick = onGoToTopic
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoToProgress() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF4CAF50))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Tiến độ của bạn", fontWeight = FontWeight.Bold)
                        Text("Xem thống kê học tập", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Hoạt động gần đây", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Dummy Recent Activity
            ActivityItem(
                title = "Chào hỏi & Giới thiệu",
                category = "Tiếng Anh Cơ Bản",
                time = "2 giờ trước"
            )
        }
    }
}

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        color = color
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 220.dp, y = (-50).dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text(subtitle, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "HỌC NGAY",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, category: String, time: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text("$category • $time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
