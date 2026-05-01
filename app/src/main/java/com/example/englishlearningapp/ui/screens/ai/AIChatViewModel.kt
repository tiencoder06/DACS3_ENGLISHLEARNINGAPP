package com.example.englishlearningapp.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.AIMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatUiState(
    val messages: List<AIMessage> = listOf(
        AIMessage(
            role = "assistant",
            content = "Xin chào! Mình có thể giúp bạn giải thích từ vựng, ngữ pháp, câu sai trong Quiz hoặc gợi ý học hôm nay."
        )
    ),
    val isThinking: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class AIChatViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val userMessage = AIMessage(role = "user", content = text)
        
        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isThinking = true
            )
        }

        viewModelScope.launch {
            // Mock delay for AI thinking
            delay(1500)
            
            val mockResponse = AIMessage(
                role = "assistant",
                content = "Đây là phản hồi mẫu. Ở phase sau, mình sẽ được kết nối với Gemini thông qua backend bảo mật."
            )
            
            _uiState.update { 
                it.copy(
                    messages = it.messages + mockResponse,
                    isThinking = false
                )
            }
        }
    }
    
    fun sendSuggestion(text: String) {
        _uiState.update { it.copy(inputText = text) }
        sendMessage()
    }
}
