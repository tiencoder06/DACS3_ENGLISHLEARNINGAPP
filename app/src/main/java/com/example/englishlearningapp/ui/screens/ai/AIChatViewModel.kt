package com.example.englishlearningapp.ui.screens.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.AIMessage
import com.example.englishlearningapp.data.repository.AIChatRepository
import com.example.englishlearningapp.data.repository.AIProxyRepository
import com.example.englishlearningapp.data.repository.UserRepository
import com.example.englishlearningapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val chatRepository: AIChatRepository,
    private val proxyRepository: AIProxyRepository,
    private val userRepository: UserRepository,
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
            chatRepository.getChatMessages(userId).collect { result ->
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
        val currentHistory = _uiState.value.messages
        
        _uiState.update { it.copy(inputText = "", isThinking = true) }

        viewModelScope.launch {
            // 1. Save User Message
            chatRepository.saveMessage(userId, userMessage)
            
            // 2. Get User Context (Level/Skill)
            var level: String? = null
            var weakSkill: String? = null
            
            val profileResult = userRepository.getUserProfile()
            if (profileResult is Resource.Success) {
                level = profileResult.data?.placementLevel
                weakSkill = profileResult.data?.placementWeakSkill
            }

            // 3. Ask Cloudflare Worker
            val response = proxyRepository.askWorker(
                userMessage = text,
                placementLevel = level,
                placementWeakSkill = weakSkill,
                recentMessages = currentHistory.takeLast(10) // Gửi 10 tin gần nhất làm context
            )

            // 4. Handle Result
            val assistantContent = when (response) {
                is Resource.Success -> response.data ?: "Không có phản hồi từ AI."
                is Resource.Error -> response.message ?: "Lỗi kết nối với AI Tutor."
                else -> "AI Tutor hiện chưa phản hồi được. Vui lòng thử lại sau."
            }

            val assistantMessage = AIMessage(
                role = "assistant",
                content = assistantContent
            )
            
            // 5. Save Assistant Message
            chatRepository.saveMessage(userId, assistantMessage)
            
            _uiState.update { it.copy(isThinking = false) }
        }
    }
    
    fun sendSuggestion(text: String) {
        onInputChange(text)
        sendMessage()
    }
}
