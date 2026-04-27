package com.example.englishlearningapp.ui.screens.progress

import androidx.lifecycle.*
import com.example.englishlearningapp.data.model.*
import com.example.englishlearningapp.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _progress = MutableStateFlow<Progress?>(null)
    val progress: StateFlow<Progress?> = _progress

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadProgress()
    }

    fun loadProgress() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val analytics = quizRepository.getUserAnalytics(userId)
                val weakWordsCount = quizRepository.getWeakWordsCount(userId)
                val userStats = quizRepository.getUserStats(userId)
                val quizResults = quizRepository.getQuizResults(userId)
                val practiceAttempts = quizRepository.getStudySessionCount(userId, "practice")
                
                if (analytics == null && quizResults.isEmpty() && practiceAttempts == 0) {
                    _progress.value = Progress()
                    return@launch
                }

                val totalAttempts = (analytics?.get("totalAttempts") as? Long)?.toInt() ?: 0
                val totalCorrect = (analytics?.get("totalCorrect") as? Long)?.toInt() ?: 0
                // Total questions across all quizzes
                // Since we don't store total questions directly in analytics in the current interface,
                // we can estimate or fetch from history. Let's assume syncUserAnalytics handles this or we sum it up.
                // For simplicity, if we don't have totalQuestions, we use attempts * 10 (MAX_QUESTIONS)
                val totalQuestions = totalAttempts * 10 
                
                val accuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0
                val bestScore = (analytics?.get("bestScore") as? Long)?.toInt() ?: 0
                val wordsLearned = (userStats?.get("wordsLearned") as? Long)?.toInt() ?: 0
                val streak = (userStats?.get("streak") as? Long)?.toInt() ?: 0
                
                val lastScore = quizResults.firstOrNull()?.score ?: 0
                val avgScore = if (totalAttempts > 0) ((analytics?.get("totalScore") as? Long)?.toInt() ?: 0) / totalAttempts else 0

                val evaluation = when {
                    accuracy >= 85 -> "Thông thái"
                    accuracy >= 70 -> "Vững vàng"
                    accuracy >= 50 -> "Đang tiến bộ"
                    else -> "Cần cố gắng nhiều"
                }

                val suggestion = when {
                    weakWordsCount > 0 -> "Bạn có $weakWordsCount từ cần cải thiện. Hãy vào phần 'Luyện tập từ khó'!"
                    accuracy < 60 -> "Hãy làm lại các bài Quiz cũ để tăng tỷ lệ chính xác."
                    totalAttempts < 5 -> "Làm thêm Quiz để hệ thống phân tích sâu hơn về năng lực của bạn."
                    else -> "Phong độ rất tốt! Hãy tiếp tục chinh phục các chủ đề mới."
                }

                _progress.value = Progress(
                    totalQuizAttempts = totalAttempts,
                    totalPracticeAttempts = practiceAttempts,
                    totalVocabulariesLearned = wordsLearned,
                    totalAnswers = totalQuestions,
                    correctAnswers = totalCorrect,
                    accuracy = accuracy,
                    weakWordsCount = weakWordsCount,
                    mistakeCount = weakWordsCount,
                    averageScore = avgScore,
                    bestScore = bestScore,
                    lastScore = lastScore,
                    streak = streak,
                    evaluation = evaluation,
                    suggestion = suggestion
                )
            } catch (e: Exception) {
                _progress.value = Progress(evaluation = "Lỗi tải dữ liệu", suggestion = e.message ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
