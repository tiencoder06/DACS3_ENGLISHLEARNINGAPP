package com.example.englishlearningapp.ui.screens.practice

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.Question
import com.example.englishlearningapp.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizSummary(
    val score: Int,
    val total: Int,
    val wrongQuestions: List<Pair<Question, String>> // Question and user's wrong answer
)

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository
) : ViewModel() {

    private val _questions = mutableStateOf<List<Question>>(emptyList())
    val questions: State<List<Question>> = _questions

    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    private val _userAnswers = mutableStateOf<Map<Int, String>>(emptyMap())
    val userAnswers: State<Map<Int, String>> = _userAnswers

    private val _isAnswerChecked = mutableStateOf(false)
    val isAnswerChecked: State<Boolean> = _isAnswerChecked

    private val _quizSummary = mutableStateOf<QuizSummary?>(null)
    val quizSummary: State<QuizSummary?> = _quizSummary

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _questions.value = repository.getQuestions()
        }
    }

    fun onAnswer(answer: String) {
        if (!_isAnswerChecked.value) {
            _userAnswers.value = _userAnswers.value + (_currentIndex.value to answer)
        }
    }

    fun checkAnswer() {
        _isAnswerChecked.value = true
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value++
            _isAnswerChecked.value = false
        } else {
            calculateResult()
        }
    }

    private fun calculateResult() {
        val correctCount = _questions.value.filterIndexed { index, question ->
            _userAnswers.value[index]?.trim()?.lowercase() == question.correctAnswer.trim().lowercase()
        }.size

        val wrongQuestions = _questions.value.mapIndexedNotNull { index, question ->
            val userAnswer = _userAnswers.value[index] ?: ""
            if (userAnswer.trim().lowercase() != question.correctAnswer.trim().lowercase()) {
                question to userAnswer
            } else null
        }

        _quizSummary.value = QuizSummary(
            score = correctCount,
            total = _questions.value.size,
            wrongQuestions = wrongQuestions
        )
    }

    fun retry() {
        _currentIndex.value = 0
        _userAnswers.value = emptyMap()
        _isAnswerChecked.value = false
        _quizSummary.value = null
        loadQuestions()
    }
}
