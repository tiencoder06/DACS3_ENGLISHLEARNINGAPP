package com.example.englishlearningapp.ui.screens.practice

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.englishlearningapp.data.model.*
import com.example.englishlearningapp.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val quizRepository: QuizRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String? = savedStateHandle["lessonId"]

    private val _vocabularies = mutableStateOf<List<Vocabulary>>(emptyList())
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

    private val _correctCount = mutableIntStateOf(0)
    val correctCount: State<Int> = _correctCount

    private val _isFinished = mutableStateOf(false)
    val isFinished: State<Boolean> = _isFinished

    private val _wrongQuestions = mutableStateListOf<PracticeQuestion>()
    val wrongQuestionsCount: Int get() = _wrongQuestions.size

    init {
        if (lessonId == "difficult_words_review") {
            loadDifficultWords()
        } else {
            loadVocabularies()
        }
    }

    private fun loadDifficultWords() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val result = vocabularyRepository.getDifficultVocabularies(userId)
                if (result.isNotEmpty()) {
                    _vocabularies.value = result.shuffled()
                    generateQuestions()
                } else {
                    _error.value = "Bạn chưa có từ khó nào để luyện tập!"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadVocabularies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentLessonId = lessonId ?: "general"
                val vocabList = vocabularyRepository.getVocabulariesByLesson(currentLessonId)
                if (vocabList.isNotEmpty()) {
                    _vocabularies.value = vocabList.filter { it.status == "active" }.shuffled()
                    generateQuestions()
                } else {
                    _error.value = "Không tìm thấy từ vựng trong bài học này."
                }
            } catch (e: Exception) {
                _error.value = "Lỗi tải dữ liệu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateQuestions() {
        val allVocabs = _vocabularies.value
        if (allVocabs.isEmpty()) return

        val questionCount = minOf(allVocabs.size, 10)
        val selectedVocabs = allVocabs.shuffled().take(questionCount)

        val generated = mutableListOf<PracticeQuestion>()
        
        selectedVocabs.forEachIndexed { index, vocab ->
            val types = listOf(
                PracticeType.EN_TO_VI,
                PracticeType.VI_TO_EN,
                PracticeType.AUDIO_TO_VI,
                PracticeType.FILL_BLANK,
                PracticeType.MATCHING
            )
            val type = types[index % types.size]

            val distractors = allVocabs.filter { it.id != vocab.id }.shuffled()
            
            when (type) {
                PracticeType.EN_TO_VI -> {
                    val options = (distractors.take(3).map { it.meaning } + vocab.meaning).shuffled()
                    generated.add(PracticeQuestion(vocab, type, options, vocab.meaning))
                }
                PracticeType.VI_TO_EN -> {
                    val options = (distractors.take(3).map { it.word } + vocab.word).shuffled()
                    generated.add(PracticeQuestion(vocab, type, options, vocab.word))
                }
                PracticeType.AUDIO_TO_VI -> {
                    val options = (distractors.take(3).map { it.meaning } + vocab.meaning).shuffled()
                    generated.add(PracticeQuestion(vocab, type, options, vocab.meaning))
                }
                PracticeType.MATCHING -> {
                    val matchingSet = (distractors.take(3) + vocab).shuffled()
                    val pairs = matchingSet.map { it.word to it.meaning }
                    generated.add(PracticeQuestion(vocab, type, matchingPairs = pairs))
                }
                else -> { // FILL_BLANK
                    generated.add(PracticeQuestion(vocab, type, correctAnswer = vocab.word))
                }
            }
        }
        _questions.value = generated.shuffled()
        
        // Track "seen" for all questions in this session
        trackQuestionsSeen(_questions.value)
    }

    private fun trackQuestionsSeen(questions: List<PracticeQuestion>) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            questions.forEach { question ->
                quizRepository.trackVocabularySeen(userId, question.vocabulary.id)
            }
        }
    }

    // Matching States
    val selectedLeft = mutableStateOf<String?>(null)
    val selectedRight = mutableStateOf<String?>(null)
    val matchedPairs = mutableStateListOf<String>()
    val wrongLeft = mutableStateOf<String?>(null)
    val wrongRight = mutableStateOf<String?>(null)

    fun onMatchingSelect(item: String, isLeft: Boolean) {
        if (_showFeedback.value) return
        
        if (isLeft) {
            selectedLeft.value = if (selectedLeft.value == item) null else item
            wrongLeft.value = null
            wrongRight.value = null
        } else {
            selectedRight.value = if (selectedRight.value == item) null else item
            wrongLeft.value = null
            wrongRight.value = null
        }

        val left = selectedLeft.value
        val right = selectedRight.value

        if (left != null && right != null) {
            val currentQuestion = _questions.value.getOrNull(_currentIndex.intValue) ?: return
            val pairs = currentQuestion.matchingPairs
            
            val isCorrectPair = pairs.any { (it.first == left && it.second == right) || (it.first == right && it.second == left) }

            if (isCorrectPair) {
                matchedPairs.add(left)
                matchedPairs.add(right)
                selectedLeft.value = null
                selectedRight.value = null
                
                if (matchedPairs.size == pairs.size * 2) {
                    _isCorrect.value = true
                    _showFeedback.value = true
                    _correctCount.value++
                }
            } else {
                wrongLeft.value = left
                wrongRight.value = right
                viewModelScope.launch {
                    kotlinx.coroutines.delay(400)
                    wrongLeft.value = null
                    wrongRight.value = null
                    selectedLeft.value = null
                    selectedRight.value = null
                }
            }
        }
    }

    fun onSelectAnswer(answer: String) {
        if (_showFeedback.value) return
        _userAnswer.value = answer
    }

    fun checkAnswer() {
        if (_showFeedback.value) return
        val userId = auth.currentUser?.uid ?: return
        val currentQuestion = _questions.value.getOrNull(_currentIndex.intValue) ?: return
        
        val correct = if (currentQuestion.type == PracticeType.FILL_BLANK) {
            _userAnswer.value.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)
        } else {
            _userAnswer.value == currentQuestion.correctAnswer
        }

        _isCorrect.value = correct
        _showFeedback.value = true
        
        if (correct) {
            _correctCount.value++
        } else {
            _wrongQuestions.add(currentQuestion)
            // Tracking light: Update difficult words on wrong answer during practice
            viewModelScope.launch {
                quizRepository.updateDifficultWords(userId, currentQuestion.vocabulary.id)
            }
        }
    }

    fun nextQuestion() {
        if (_currentIndex.intValue < _questions.value.size - 1) {
            _currentIndex.intValue++
            resetState()
        } else {
            _isFinished.value = true
        }
    }

    private fun resetState() {
        _showFeedback.value = false
        _userAnswer.value = ""
        _isCorrect.value = false
        matchedPairs.clear()
        selectedLeft.value = null
        selectedRight.value = null
    }

    fun retry() {
        _currentIndex.intValue = 0
        _correctCount.value = 0
        _isFinished.value = false
        _wrongQuestions.clear()
        resetState()
        if (lessonId == "difficult_words_review") loadDifficultWords() else loadVocabularies()
    }

    fun retryMistakes() {
        val mistakes = _wrongQuestions.toList().shuffled()
        _questions.value = mistakes
        _wrongQuestions.clear()
        _currentIndex.intValue = 0
        _correctCount.value = 0
        _isFinished.value = false
        resetState()
    }
}
