package com.example.englishlearningapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.data.repository.AuthRepository
import com.example.englishlearningapp.data.repository.UserRepository
import com.example.englishlearningapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userProfile: StateFlow<Resource<User>> = _userProfile

    private val _updateState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateState: StateFlow<Resource<Boolean>?> = _updateState

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfile.value = Resource.Loading
            _userProfile.value = userRepository.getUserProfile()
        }
    }

    fun updateProfile(newName: String) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = userRepository.updateFullName(newName)
            _updateState.value = result
            if (result is Resource.Success) {
                fetchUserProfile()
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }

    fun logout() {
        authRepository.logout()
    }
}
