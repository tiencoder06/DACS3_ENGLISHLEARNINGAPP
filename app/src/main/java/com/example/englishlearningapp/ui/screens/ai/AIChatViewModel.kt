package com.example.englishlearningapp.ui.screens.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.AIMessage
import com.example.englishlearningapp.data.repository.AIChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatUiState(
    val messages: List<AIMessage> = emptyList(),
    val isThinking: Boolean = false,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val userId: String? = null
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val repository: AIChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    private var chatJob: Job? = null
    private val welcomeMessage = AIMessage(
        role = "assistant",
        content = "Xin chào! Mình có thể giúp bạn giải thích từ vựng, ngữ pháp, câu sai trong Quiz hoặc gợi ý học hôm nay."
    )

    fun initChat() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Nếu chuyển sang User khác, xóa dữ liệu cũ và bắt đầu lắng nghe User mới
        if (_uiState.value.userId != currentUserId) {
            _uiState.update { 
                it.copy(
                    messages = emptyList(), 
                    userId = currentUserId, 
                    isLoading = true 
                ) 
            }
            startListeningToChat(currentUserId)
        }
    }

    private fun startListeningToChat(userId: String) {
        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            repository.getChatMessages(userId).collect { result ->
                result.onSuccess { historicalMessages ->
                    _uiState.update { 
                        it.copy(
                            messages = if (historicalMessages.isEmpty()) listOf(welcomeMessage) else historicalMessages,
                            isLoading = false
                        )
                    }
                }.onFailure { e ->
                    Log.e("AIChatViewModel", "Error subscribing to chat", e)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val userId = auth.currentUser?.uid ?: return
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val userMessage = AIMessage(role = "user", content = text)
        
        // Xóa text input và hiện trạng thái "đang suy nghĩ"
        _uiState.update { it.copy(inputText = "", isThinking = true) }

        viewModelScope.launch {
            // 1. Lưu tin nhắn của User vào Firestore
            repository.saveMessage(userId, userMessage)
            
            // 2. Giả lập AI suy nghĩ
            delay(1500)
            
            val mockResponse = AIMessage(
                role = "assistant",
                content = "Đây là phản hồi mẫu. Ở phase sau, mình sẽ được kết nối với Gemini thông qua backend bảo mật."
            )
            
            // 3. Lưu phản hồi AI vào Firestore
            repository.saveMessage(userId, mockResponse)
            
            _uiState.update { it.copy(isThinking = false) }
        }
    }
    
    fun sendSuggestion(text: String) {
        onInputChange(text)
        sendMessage()
    }
}
