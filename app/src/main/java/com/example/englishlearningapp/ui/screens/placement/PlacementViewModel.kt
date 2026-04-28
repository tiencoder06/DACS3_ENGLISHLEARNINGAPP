package com.example.englishlearningapp.ui.screens.placement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.PlacementQuestion
import com.example.englishlearningapp.data.model.PlacementResult
import com.example.englishlearningapp.data.repository.PlacementRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlacementUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val questions: List<PlacementQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String = "",
    val userAnswers: Map<String, String> = emptyMap(),
    val result: PlacementResult? = null,
    val isCompleted: Boolean = false
)

@HiltViewModel
class PlacementViewModel @Inject constructor(
    private val repository: PlacementRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacementUiState())
    val uiState: StateFlow<PlacementUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getPlacementQuestions().collect { result ->
                result.fold(
                    onSuccess = { questions ->
                        if (questions.isEmpty()) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    errorMessage = "Không có dữ liệu bài kiểm tra." 
                                ) 
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    questions = questions,
                                    currentQuestionIndex = 0,
                                    selectedAnswer = ""
                                ) 
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = "Lỗi tải câu hỏi: ${error.localizedMessage}" 
                            ) 
                        }
                    }
                )
            }
        }
    }

    fun selectAnswer(answer: String) {
        val currentQuestion = _uiState.value.questions.getOrNull(_uiState.value.currentQuestionIndex)
        if (currentQuestion != null) {
            _uiState.update { 
                it.copy(
                    selectedAnswer = answer,
                    userAnswers = it.userAnswers + (currentQuestion.questionId to answer)
                ) 
            }
        }
    }

    fun goToNextQuestion() {
        val state = _uiState.value
        if (state.selectedAnswer.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn một đáp án.") }
            return
        }

        if (state.currentQuestionIndex < state.questions.size - 1) {
            val nextIndex = state.currentQuestionIndex + 1
            val nextSelectedAnswer = state.userAnswers[state.questions[nextIndex].questionId] ?: ""
            _uiState.update { 
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = nextSelectedAnswer,
                    errorMessage = null
                ) 
            }
        }
    }

    fun goToPreviousQuestion() {
        val state = _uiState.value
        if (state.currentQuestionIndex > 0) {
            val prevIndex = state.currentQuestionIndex - 1
            val prevSelectedAnswer = state.userAnswers[state.questions[prevIndex].questionId] ?: ""
            _uiState.update { 
                it.copy(
                    currentQuestionIndex = prevIndex,
                    selectedAnswer = prevSelectedAnswer,
                    errorMessage = null
                ) 
            }
        }
    }

    fun submitPlacement() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy tài khoản người dùng.") }
            return
        }

        val state = _uiState.value
        if (state.userAnswers.size < state.questions.size) {
            _uiState.update { it.copy(errorMessage = "Vui lòng hoàn thành tất cả các câu hỏi.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.calculatePlacementResult(state.questions, state.userAnswers)
            val saveResult = repository.savePlacementResult(userId, result)
            
            saveResult.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            result = result,
                            isCompleted = true
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            errorMessage = "Lỗi lưu kết quả: ${error.localizedMessage}" 
                        ) 
                    }
                }
            )
        }
    }

    fun skipPlacement() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy tài khoản người dùng.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val skipResult = repository.skipPlacement(userId)
            skipResult.fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            isCompleted = true
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            errorMessage = "Lỗi khi bỏ qua bài kiểm tra: ${error.localizedMessage}" 
                        ) 
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
