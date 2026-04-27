package com.example.englishlearningapp.ui.screens.quiz

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.englishlearningapp.data.model.*
import com.example.englishlearningapp.data.repository.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    private val QUIZ_TIME_LIMIT = 300 // 5 minutes (300 seconds)

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

    // Timer states
    private var timerJob: Job? = null
    private val _timeLeft = mutableIntStateOf(QUIZ_TIME_LIMIT)
    val timeLeft: State<Int> = _timeLeft

    private val _isTimeUp = mutableStateOf(false)
    val isTimeUp: State<Boolean> = _isTimeUp

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
                    _error.value = "Không tìm thấy dữ liệu bài kiểm tra."
                    return@launch
                }

                _questions.value = filtered
                    .shuffled()
                    .take(MAX_QUESTIONS)
                    .map { it.copy(options = it.options?.shuffled()) }
                
                startTimer()
            } catch (e: Exception) {
                _error.value = "Lỗi tải câu hỏi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timeLeft.intValue = QUIZ_TIME_LIMIT
        _isTimeUp.value = false
        timerJob = viewModelScope.launch {
            while (_timeLeft.intValue > 0 && isActive) {
                delay(1000)
                _timeLeft.intValue--
            }
            if (_timeLeft.intValue <= 0 && isActive) {
                _isTimeUp.value = true
                submitQuiz()
            }
        }
    }

    fun onAnswer(answer: String) {
        if (_isTimeUp.value) return
        _userAnswers.value = _userAnswers.value + (_currentIndex.intValue to answer)
    }

    fun nextQuestion() {
        if (_currentIndex.intValue < _questions.value.size - 1) {
            _currentIndex.intValue++
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val questionsList = _questions.value
        val correctCount = questionsList.filterIndexed { index, q ->
            _userAnswers.value[index]?.trim()?.equals(q.correctAnswer.trim(), ignoreCase = true) == true
        }.size

        val total = questionsList.size
        val score = if (total > 0) (correctCount * 100) / total else 0

        val wrong = questionsList.mapIndexedNotNull { index, q ->
            val ans = _userAnswers.value[index] ?: ""
            if (!ans.trim().equals(q.correctAnswer.trim(), ignoreCase = true)) q to ans else null
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
        val currentLessonId = lessonId ?: "general"

        viewModelScope.launch {
            try {
                // 1. Save to quiz_results collection
                val quizResult = QuizResult(
                    userId = userId,
                    lessonId = currentLessonId,
                    type = "quiz",
                    score = score,
                    correctCount = correct,
                    totalQuestions = total,
                    submittedAt = Timestamp.now()
                )
                quizRepository.saveQuizResult(quizResult)

                // 2. Update learning_progress
                quizRepository.updateLearningProgress(userId, currentLessonId, score)

                // 3. Update difficult_words for wrong answers
                wrong.forEach { (question, _) ->
                    if (question.vocabId.isNotEmpty()) {
                        quizRepository.updateDifficultWords(userId, question.vocabId)
                    }
                }

                // 4. Update general analytics and mastery
                quizRepository.syncUserAnalytics(userId, score, correct)
                _questions.value.forEachIndexed { index, q ->
                    if (q.vocabId.isNotEmpty()) {
                        val isCorrect = !wrong.any { it.first.id == q.id }
                        quizRepository.updateWordMastery(userId, q.vocabId, isCorrect)
                    }
                }

                quizRepository.logStudySession(userId, "quiz", score, correct, total)

            } catch (e: Exception) {
                Log.e("QUIZ_SAVE", "Error saving results: ${e.message}")
            }
        }
    }

    fun retry() {
        _currentIndex.intValue = 0
        _userAnswers.value = emptyMap()
        _quizResult.value = null
        _isTimeUp.value = false
        _timeLeft.intValue = QUIZ_TIME_LIMIT
        loadQuestions()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
