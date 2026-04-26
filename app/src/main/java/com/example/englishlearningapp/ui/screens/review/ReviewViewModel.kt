package com.example.englishlearningapp.ui.screens.review

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.englishlearningapp.data.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewManager @Inject constructor() {
    private var _wrongQuestions: List<Question> = emptyList()
    val wrongQuestions: List<Question> get() = _wrongQuestions

    fun setQuestions(questions: List<Question>) {
        _wrongQuestions = questions
    }
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewManager: ReviewManager
) : ViewModel() {

    private val _questions = mutableStateOf<List<Question>>(reviewManager.wrongQuestions)
    val questions: State<List<Question>> = _questions

    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    private val _userAnswer = mutableStateOf("")
    val userAnswer: State<String> = _userAnswer

    private val _isAnswered = mutableStateOf(false)
    val isAnswered: State<Boolean> = _isAnswered

    private val _isCorrect = mutableStateOf(false)
    val isCorrect: State<Boolean> = _isCorrect

    private val _correctCount = mutableStateOf(0)
    val correctCount: State<Int> = _correctCount

    private val _isFinished = mutableStateOf(false)
    val isFinished: State<Boolean> = _isFinished

    fun setQuestions(questions: List<Question>) {
        _questions.value = questions
        reset()
    }

    fun onAnswerChange(answer: String) {
        if (!_isAnswered.value) {
            _userAnswer.value = answer
        }
    }

    fun checkAnswer() {
        val currentQuestion = _questions.value.getOrNull(_currentIndex.value) ?: return
        _isAnswered.value = true
        val correct = _userAnswer.value.trim().lowercase() == currentQuestion.correctAnswer.trim().lowercase()
        _isCorrect.value = correct
        if (correct) {
            _correctCount.value += 1
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value += 1
            _userAnswer.value = ""
            _isAnswered.value = false
            _isCorrect.value = false
        } else {
            _isFinished.value = true
        }
    }

    fun reset() {
        _currentIndex.value = 0
        _userAnswer.value = ""
        _isAnswered.value = false
        _isCorrect.value = false
        _correctCount.value = 0
        _isFinished.value = false
    }
}
