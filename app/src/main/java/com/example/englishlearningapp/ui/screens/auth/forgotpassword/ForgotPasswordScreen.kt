package com.example.englishlearningapp.ui.screens.auth.forgotpassword

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.ui.screens.auth.AuthViewModel
import com.example.englishlearningapp.utils.Resource
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is Resource.Success && !hasNavigated) {
            Toast.makeText(
                context,
                "Đã gửi email. Vui lòng kiểm tra hộp thư",
                Toast.LENGTH_LONG
            ).show()
            delay(3000)
            if (!hasNavigated) {
                hasNavigated = true
                onBackToLogin()
                viewModel.resetState()
            }
        } else if (authState is Resource.Error) {
            Toast.makeText(
                context,
                (authState as Resource.Error).message,
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LingoPro",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004ac6)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!hasNavigated) {
                            hasNavigated = true
                            onBackToLogin()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFfaf8ff)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Column {
                        HeaderArea()
                        ContentArea(
                            email = email,
                            onEmailChange = { email = it },
                            authState = authState,
                            onResetClick = {
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    viewModel.forgotPassword(email)
                                } else {
                                    Toast.makeText(context, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Footer(onBackClick = {
                    if (!hasNavigated) {
                        hasNavigated = true
                        onBackToLogin()
                    }
                })
            }
        }
    }
}

@Composable
fun HeaderArea() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFdbe1ff), Color(0xFF6cf8bb))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF004ac6)
                )
            }
        }
    }
}

@Composable
fun ContentArea(
    email: String,
    onEmailChange: (String) -> Unit,
    authState: Resource<Boolean>?,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Quên mật khẩu",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1a1c1e)
        )
        Text(
            text = "Nhập email để nhận liên kết khôi phục",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Địa chỉ Email") },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFe1e2ec),
                focusedBorderColor = Color(0xFF004ac6)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onResetClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004ac6)),
            enabled = authState !is Resource.Loading && email.isNotEmpty()
        ) {
            if (authState is Resource.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Gửi liên kết", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun Footer(onBackClick: () -> Unit) {
    TextButton(
        onClick = onBackClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quay lại Đăng nhập",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
