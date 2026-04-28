package com.example.englishlearningapp.ui.screens.auth.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.englishlearningapp.ui.screens.auth.AuthViewModel
import com.example.englishlearningapp.utils.Resource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is Resource.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is Resource.Error -> {
                Toast.makeText(context, (authState as Resource.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome Back", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = onGoToForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (authState is Resource.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotEmpty() && password.isNotEmpty()
            ) {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onGoToRegister) {
            Text("Don't have an account? Register Now")
        }
    }
}
