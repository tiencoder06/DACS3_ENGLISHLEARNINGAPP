package com.example.englishlearningapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.ui.navigation.Routes
import com.example.englishlearningapp.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onEditProfileClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onNavigateToPlacement: () -> Unit
) {
    val profileState by viewModel.userProfile.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất?") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLogoutSuccess()
                }) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Linguist",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004ac6)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notification */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFfaf8ff)
                )
            )
        },
        containerColor = Color(0xFFfaf8ff)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (profileState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Success -> {
                    val user = (profileState as Resource.Success).data
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            ProfileHeader(user = user, onEditClick = onEditProfileClick)
                        }

                        // Placement Test Result Section
                        item {
                            PlacementResultCard(
                                user = user,
                                onRetakeClick = onNavigateToPlacement
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    StatCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Chuỗi hiện tại",
                                        value = "${user.streakDays} ngày",
                                        icon = Icons.Default.LocalFireDepartment,
                                        iconColor = Color(0xFFFF9800)
                                    )
                                    StatCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Kỷ lục dài nhất",
                                        value = "${user.longestStreak} ngày",
                                        icon = Icons.Default.EmojiEvents,
                                        iconColor = Color(0xFFFFD700)
                                    )
                                }
                                StatCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Từ đã học",
                                    value = "${user.wordsLearned} từ",
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = Color(0xFF004ac6)
                                )
                            }
                        }

                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    MenuItem("Thông tin cá nhân", Icons.Default.Person, onEditProfileClick)
                                    MenuItem("Bảo mật", Icons.Default.Lock, onSecurityClick)
                                    MenuItem("Thông báo nhắc nhở", Icons.Default.Notifications, onSettingsClick)
                                    MenuItem("Trợ giúp & Giới thiệu", Icons.Default.Info, {})
                                }
                            }
                        }

                        item {
                            SignOutButton(onClick = { showLogoutDialog = true })
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = (profileState as Resource.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun PlacementResultCard(user: User, onRetakeClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF004ac6).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF004ac6))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trình độ tiếng Anh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.placementCompleted) {
                        Text(
                            text = "Dựa trên bài kiểm tra đầu vào",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (user.placementCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = user.placementLevel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF004ac6)
                        )
                        Text(
                            text = "Điểm số: ${user.placementScore}/100",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    
                    if (user.placementTakenAt != null) {
                        val date = user.placementTakenAt.toDate()
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = "Ngày thi: ${sdf.format(date)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }

                if (!user.placementSkipped && user.placementStrongSkill.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SkillLabel(label = "Mạnh: ${formatSkillName(user.placementStrongSkill)}", isStrong = true)
                        if (user.placementWeakSkill.isNotEmpty() && user.placementWeakSkill != "Balanced") {
                            SkillLabel(label = "Cần học: ${formatSkillName(user.placementWeakSkill)}", isStrong = false)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onRetakeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004ac6).copy(alpha = 0.05f), contentColor = Color(0xFF004ac6)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Làm lại kiểm tra", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Bạn chưa kiểm tra trình độ đầu vào để nhận lộ trình học phù hợp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetakeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kiểm tra ngay", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SkillLabel(label: String, isStrong: Boolean) {
    Surface(
        color = if (isStrong) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isStrong) Color(0xFF2E7D32) else Color(0xFFE65100),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatSkillName(skill: String): String {
    return when (skill) {
        "vocabulary_grammar" -> "Từ vựng"
        "listening" -> "Nghe"
        "sentence_usage" -> "Ứng dụng"
        "Balanced" -> "Cân bằng"
        else -> skill
    }
}

@Composable
fun ProfileHeader(user: User, onEditClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(120.dp)) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF004ac6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.fullName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Edit Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clickable { onEditClick() },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    modifier = Modifier.padding(8.dp),
                    tint = Color(0xFF004ac6)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = Color(0xFF006c49).copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = user.level,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color(0xFF006c49),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFfaf8ff)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = Color(0xFF004ac6)
                )
            }
        },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SignOutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Red))
    ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Đăng xuất", fontWeight = FontWeight.Bold)
    }
}
