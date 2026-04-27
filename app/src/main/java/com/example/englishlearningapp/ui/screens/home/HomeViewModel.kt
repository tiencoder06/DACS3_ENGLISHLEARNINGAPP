package com.example.englishlearningapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.Progress
import com.example.englishlearningapp.data.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _progress = MutableStateFlow<Progress?>(null)
    val progress: StateFlow<Progress?> = _progress

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val analytics = quizRepository.getUserAnalytics(userId)
                val weakWordsCount = quizRepository.getWeakWordsCount(userId)
                val userStats = quizRepository.getUserStats(userId)
                val quizResults = quizRepository.getQuizResults(userId)

                val totalAttempts = (analytics?.get("totalAttempts") as? Long)?.toInt() ?: 0
                val totalCorrect = (analytics?.get("totalCorrect") as? Long)?.toInt() ?: 0
                val totalQuestions = totalAttempts * 10
                val accuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0
                val wordsLearned = (userStats?.get("wordsLearned") as? Long)?.toInt() ?: 0

                _progress.value = Progress(
                    totalVocabulariesLearned = wordsLearned,
                    accuracy = accuracy,
                    weakWordsCount = weakWordsCount,
                    totalQuizAttempts = totalAttempts,
                    lastScore = quizResults.firstOrNull()?.score ?: 0
                )
            } catch (e: Exception) {
                // Error handling
            } finally {
                _isLoading.value = false
            }
        }
    }
}
