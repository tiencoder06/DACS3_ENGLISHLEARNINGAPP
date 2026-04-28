package com.example.englishlearningapp.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()
    val context = LocalContext.current

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute -> viewModel.updateReminderTime(hour, minute) },
        reminderHour,
        reminderMinute,
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt thông báo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFfaf8ff))
            )
        },
        containerColor = Color(0xFFfaf8ff)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Nhắc nhở học tập",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF004ac6),
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Nhắc nhở hàng ngày") },
                            supportingContent = { Text("Thông báo nhắc bạn học tập mỗi ngày") },
                            leadingContent = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF004ac6)) },
                            trailingContent = {
                                Switch(
                                    checked = dailyReminderEnabled,
                                    onCheckedChange = { viewModel.toggleDailyReminder(it) }
                                )
                            }
                        )
                        
                        if (dailyReminderEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            ListItem(
                                headlineContent = { Text("Thời gian nhắc nhở") },
                                supportingContent = { 
                                    val time = String.format(Locale.getDefault(), "%02d:%02d", reminderHour, reminderMinute)
                                    Text(time) 
                                },
                                leadingContent = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF004ac6)) },
                                trailingContent = { 
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) 
                                },
                                modifier = Modifier.clickable { timePickerDialog.show() }
                            )
                        }
                    }
                }
            }

            // Đưa nút Test trở lại theo yêu cầu của người dùng
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.testNotificationImmediate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006c49)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gửi thông báo ngay bây giờ (Test)")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ứng dụng sẽ tự động kiểm tra tiến độ và chỉ gửi thông báo nếu hôm nay bạn chưa hoàn thành bài học nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
