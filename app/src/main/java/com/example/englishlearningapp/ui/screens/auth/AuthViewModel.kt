package com.example.englishlearningapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.repository.AuthRepository
import com.example.englishlearningapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Boolean>?>(null)
    val authState: StateFlow<Resource<Boolean>?> = _authState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.login(email, pass)
        }
    }

    fun register(email: String, pass: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.register(email, pass, fullName)
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.forgotPassword(email)
        }
    }

    fun resetState() {
        _authState.value = null
    }
}
