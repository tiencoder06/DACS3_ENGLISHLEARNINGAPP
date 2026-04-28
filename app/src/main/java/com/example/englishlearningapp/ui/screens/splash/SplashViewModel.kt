package com.example.englishlearningapp.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.repository.UserRepository
import com.example.englishlearningapp.ui.navigation.Routes
import com.example.englishlearningapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<String?>(null)
    val destination = _destination.asStateFlow()

    fun checkDestination() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _destination.value = Routes.LOGIN
            } else {
                when (val result = userRepository.getUserProfile()) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user?.placementCompleted == true) {
                            _destination.value = Routes.HOME
                        } else {
                            _destination.value = Routes.PLACEMENT_INTRO
                        }
                    }
                    is Resource.Error -> {
                        // If profile fails to load, might be a network issue or missing doc
                        // For safety, if user is logged in but no profile, try to go to Login or Intro
                        _destination.value = Routes.LOGIN
                    }
                    else -> {}
                }
            }
        }
    }
}
