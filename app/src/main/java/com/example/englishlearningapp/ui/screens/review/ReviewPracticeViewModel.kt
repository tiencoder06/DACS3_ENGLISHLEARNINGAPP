package com.example.englishlearningapp.ui.screens.review

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.PracticeQuestion
import com.example.englishlearningapp.data.model.PracticeType
import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.data.repository.QuizRepository
import com.example.englishlearningapp.data.repository.VocabularyRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewPracticeViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _questions = mutableStateOf<List<PracticeQuestion>>(emptyList())
    val questions: State<List<PracticeQuestion>> = _questions

    private val _currentIndex = mutableIntStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _userAnswer = mutableStateOf("")
    val userAnswer: State<String> = _userAnswer

    private val _showFeedback = mutableStateOf(false)
    val showFeedback: State<Boolean> = _showFeedback

    private val _isCorrect = mutableStateOf(false)
    val isCorrect: State<Boolean> = _isCorrect

    private val _isFinished = mutableStateOf(false)
    val isFinished: State<Boolean> = _isFinished

    private val _correctCount = mutableIntStateOf(0)
    val correctCount: State<Int> = _correctCount

    init {
        loadReviewQuestions()
    }

    fun loadReviewQuestions() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Load vocab list from difficult_words collection
                val vocabList = vocabularyRepository.getDifficultVocabularies(userId)
                if (vocabList.isNotEmpty()) {
                    // 3. Create practice session with 5-10 questions using only wrong vocabs
                    generateQuestions(vocabList.shuffled().take(10))
                } else {
                    _error.value = "Bạn không có lỗi sai nào để ôn tập!"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateQuestions(vocabs: List<Vocabulary>) {
        val generated = vocabs.mapIndexed { index, vocab ->
            val types = listOf(
                PracticeType.EN_TO_VI,
                PracticeType.VI_TO_EN,
                PracticeType.FILL_BLANK,
                PracticeType.AUDIO_TO_VI
            )
            val type = types[index % types.size]

            val distractors = vocabs.filter { it.id != vocab.id }.shuffled()

            when (type) {
                PracticeType.EN_TO_VI -> {
                    val options = (distractors.take(3).map { it.meaning } + vocab.meaning).shuffled()
                    PracticeQuestion(vocab, type, options, vocab.meaning)
                }
                PracticeType.VI_TO_EN -> {
                    val options = (distractors.take(3).map { it.word } + vocab.word).shuffled()
                    PracticeQuestion(vocab, type, options, vocab.word)
                }
                PracticeType.AUDIO_TO_VI -> {
                    val options = (distractors.take(3).map { it.meaning } + vocab.meaning).shuffled()
                    PracticeQuestion(vocab, type, options, vocab.meaning)
                }
                else -> { // FILL_BLANK
                    PracticeQuestion(vocab, type, correctAnswer = vocab.word)
                }
            }
        }
        _questions.value = generated
    }

    fun onAnswer(answer: String) {
        if (_showFeedback.value) return
        _userAnswer.value = answer
        
        val currentQuestion = _questions.value.getOrNull(_currentIndex.intValue) ?: return
        // Allow user to see choice before clicking "Check" if it's multiple choice
        // But for consistency with your previous "Check button" request, I'll keep it manual
    }

    fun checkAnswer() {
        if (_showFeedback.value) return
        val currentQuestion = _questions.value.getOrNull(_currentIndex.intValue) ?: return
        
        val correct = _userAnswer.value.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)
        _isCorrect.value = correct
        _showFeedback.value = true
        
        if (correct) _correctCount.value++
        
        // 4 & 5. Update wrongCount and handle deletion if wrongCount reaches 0
        updateProgress(correct, currentQuestion.vocabulary.id)
    }

    private fun updateProgress(correct: Boolean, vocabId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            // This repository method already handles: 
            // - Correct -> decrease wrongCount, delete if 0
            // - Wrong -> increase wrongCount
            quizRepository.updateDifficultWordReview(userId, vocabId, correct)
        }
    }

    fun nextQuestion() {
        if (_currentIndex.intValue < _questions.value.size - 1) {
            _currentIndex.intValue++
            _showFeedback.value = false
            _userAnswer.value = ""
            _isCorrect.value = false
        } else {
            _isFinished.value = true
        }
    }

    fun retry() {
        _currentIndex.intValue = 0
        _correctCount.value = 0
        _isFinished.value = false
        _showFeedback.value = false
        _userAnswer.value = ""
        loadReviewQuestions()
    }
}
