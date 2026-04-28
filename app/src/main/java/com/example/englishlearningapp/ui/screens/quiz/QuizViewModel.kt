package com.example.englishlearningapp.ui.screens.quiz

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.englishlearningapp.data.model.*
import com.example.englishlearningapp.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizResultData(
    val score: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val wrongQuestions: List<Pair<Question, String>>
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String? = savedStateHandle["lessonId"]
    private val MAX_QUESTIONS = 10

    private val _questions = mutableStateOf<List<Question>>(emptyList())
    val questions: State<List<Question>> = _questions

    private val _currentIndex = mutableIntStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    private val _userAnswers = mutableStateOf<Map<Int, String>>(emptyMap())
    val userAnswers: State<Map<Int, String>> = _userAnswers

    private val _quizResult = mutableStateOf<QuizResultData?>(null)
    val quizResult: State<QuizResultData?> = _quizResult

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentLessonId = lessonId ?: "general"
                val result = repository.getQuestionsByLesson(currentLessonId)
                
                val filtered = result.filter {
                    it.usage == "quiz" || it.usage == "both"
                }

                if (filtered.isEmpty()) {
                    _error.value = "No quiz data found"
                    return@launch
                }

                _questions.value = filtered
                    .shuffled()
                    .take(MAX_QUESTIONS)
                    .map { it.copy(options = it.options?.shuffled()) }
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAnswer(answer: String) {
        _userAnswers.value = _userAnswers.value + (_currentIndex.intValue to answer)
    }

    fun nextQuestion() {
        if (_currentIndex.intValue < _questions.value.size - 1) {
            _currentIndex.intValue++
        }
    }

    fun submitQuiz() {
        val questionsList = _questions.value
        val correctCount = questionsList.filterIndexed { index, q ->
            _userAnswers.value[index] == q.correctAnswer
        }.size

        val total = questionsList.size
        val score = if (total > 0) (correctCount * 100) / total else 0

        val wrong = questionsList.mapIndexedNotNull { index, q ->
            val ans = _userAnswers.value[index] ?: ""
            if (ans != q.correctAnswer) q to ans else null
        }

        _quizResult.value = QuizResultData(score, correctCount, total, wrong)
        saveResult(score, correctCount, total, wrong)
    }

    private fun saveResult(
        score: Int,
        correct: Int,
        total: Int,
        wrong: List<Pair<Question, String>>
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // 1. Sync Analytics (Cập nhật bảng user_analytics)
                quizRepository.syncUserAnalytics(userId, score, correct)
                
                // 2. Log Session (Lưu vào bảng study_sessions)
                quizRepository.logStudySession(userId, "quiz", score, correct, total)
                
                // 3. Update Word Mastery (Cập nhật word_mastery cho từng câu)
                _questions.value.forEachIndexed { index, q ->
                    val vocabId = q.vocabId
                    if (!vocabId.isNullOrEmpty()) {
                        val isCorrect = _userAnswers.value[index] == q.correctAnswer
                        quizRepository.updateWordMastery(userId, vocabId, isCorrect)
                    }
                }

                // Giữ lại hàm cũ nếu cần cho các tính năng khác
                val difficultWordIds = wrong.mapNotNull { it.first.vocabId }.filter { it.isNotEmpty() }
                quizRepository.updateProgress(
                    userId = userId,
                    score = score,
                    correctCount = correct,
                    difficultWords = difficultWordIds
                )
            } catch (e: Exception) {
                Log.e("QUIZ_DEBUG", "Error in saveResult: ${e.message}")
            }
        }
    }

    fun retry() {
        _currentIndex.intValue = 0
        _userAnswers.value = emptyMap()
        _quizResult.value = null
        loadQuestions()
    }
}
