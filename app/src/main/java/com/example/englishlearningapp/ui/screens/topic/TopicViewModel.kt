package com.example.englishlearningapp.ui.screens.topic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.Topic
import com.example.englishlearningapp.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TopicUiState {
    object Loading : TopicUiState()
    data class Success(val topics: List<Topic>) : TopicUiState()
    data class Error(val message: String) : TopicUiState()
}

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val repository: TopicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopicUiState>(TopicUiState.Loading)
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    init {
        fetchTopics()
    }

    fun fetchTopics() {
        viewModelScope.launch {
            Log.d("Debug_Topic", "ViewModel fetching...")
            _uiState.value = TopicUiState.Loading
            try {
                val result = repository.getTopics()
                _uiState.value = TopicUiState.Success(result)
                Log.d("Debug_Topic", "ViewModel state updated to: Success(${result.size} topics)")
            } catch (e: Exception) {
                _uiState.value = TopicUiState.Error(e.message ?: "Unknown error occurred")
                Log.e("Debug_Topic", "ViewModel state updated to: Error(${e.message})")
            }
        }
    }
}
