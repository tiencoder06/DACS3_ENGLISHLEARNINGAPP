package com.example.englishlearningapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val profileState by viewModel.userProfile.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }

    // Cập nhật fullName khi dữ liệu profile load thành công
    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            fullName = (profileState as Resource.Success).data.fullName
        }
    }

    // Xử lý kết quả cập nhật
    LaunchedEffect(updateState) {
        when (updateState) {
            is Resource.Success -> {
                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                onBack()
                viewModel.resetUpdateState()
            }
            is Resource.Error -> {
                Toast.makeText(context, (updateState as Resource.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (updateState is Resource.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.updateProfile(fullName) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fullName.isNotEmpty()
                ) {
                    Text("Lưu thay đổi")
                }
            }
        }
    }
}
