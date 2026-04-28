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
                // 1. Lấy dữ liệu từ bảng mới 'user_analytics'
                val analytics = quizRepository.getUserAnalytics(userId)
                
                // 2. Lấy số lượng từ yếu từ bảng mới 'word_mastery'
                val weakWordsCount = quizRepository.getWeakWordsCount(userId)
                
                // 3. Lấy thông tin cơ bản từ profile 'users'
                val userStats = quizRepository.getUserStats(userId)
                
                // 4. Lấy kết quả quiz cuối cùng từ bảng 'quiz_results' hoặc 'study_sessions'
                val lastResults = quizRepository.getQuizResults(userId)
                val lastScore = lastResults.firstOrNull()?.score ?: 0

                if (analytics == null) {
                    _progress.value = Progress(
                        evaluation = "Chào mừng bạn!",
                        suggestion = "Bắt đầu học và hoàn thành bài tập đầu tiên để xem phân tích tiến độ nhé."
                    )
                    return@launch
                }

                val totalAttempts = (analytics["totalAttempts"] as? Long)?.toInt() ?: 0
                val totalScore = (analytics["totalScore"] as? Long)?.toInt() ?: 0
                val totalCorrect = (analytics["totalCorrect"] as? Long)?.toInt() ?: 0
                val bestScore = (analytics["bestScore"] as? Long)?.toInt() ?: 0
                val wordsLearned = (userStats?.get("wordsLearned") as? Long)?.toInt() ?: 0

                val averageScore = if (totalAttempts > 0) totalScore / totalAttempts else 0

                val evaluation = when {
                    averageScore >= 80 -> "Xuất sắc"
                    averageScore >= 50 -> "Khá"
                    else -> "Cần cố gắng"
                }

                val suggestion = when {
                    weakWordsCount > 5 -> "Bạn đang có $weakWordsCount từ vựng gặp khó khăn. Hãy dành thời gian ôn lại bảng 'Từ khó' nhé!"
                    averageScore >= 80 -> "Bạn đang học rất hiệu quả. Hãy thử sức với các bài học cấp độ cao hơn!"
                    else -> "Luyện tập ít nhất 15 phút mỗi ngày sẽ giúp bạn cải thiện điểm trung bình."
                }

                _progress.value = Progress(
                    totalQuizAttempts = totalAttempts,
                    totalVocabulariesLearned = wordsLearned,
                    averageScore = averageScore,
                    bestScore = bestScore,
                    lastScore = lastScore,
                    totalCorrectAnswers = totalCorrect,
                    mistakeCount = weakWordsCount,
                    evaluation = evaluation,
                    suggestion = suggestion
                )
            } catch (e: Exception) {
                _progress.value = Progress(
                    evaluation = "Lỗi",
                    suggestion = "Không thể kết nối đến dữ liệu phân tích."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
