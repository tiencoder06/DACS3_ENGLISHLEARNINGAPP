package com.example.englishlearningapp.ui.screens.practice

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.englishlearningapp.data.model.*
import com.example.englishlearningapp.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    // UI States
    private val _userAnswer = mutableStateOf("")
    val userAnswer: State<String> = _userAnswer

    private val _isAnswered = mutableStateOf(false)
    val isAnswered: State<Boolean> = _isAnswered

    private val _isCorrect = mutableStateOf(false)
    val isCorrect: State<Boolean> = _isCorrect

    private val _correctCount = mutableIntStateOf(0)
    val correctCount: State<Int> = _correctCount

    private val _isFinished = mutableStateOf(false)
    val isFinished: State<Boolean> = _isFinished

    private val _wrongQuestions = mutableStateListOf<PracticeQuestion>()
    val wrongQuestionsCount: Int get() = _wrongQuestions.size

    // Matching State
    private val _selectedLeft = mutableStateOf<String?>(null)
    val selectedLeft: State<String?> = _selectedLeft
    private val _selectedRight = mutableStateOf<String?>(null)
    val selectedRight: State<String?> = _selectedRight
    private val _matchedPairs = mutableStateListOf<String>()
    val matchedPairs: List<String> = _matchedPairs
    
    private val _wrongLeft = mutableStateOf<String?>(null)
    val wrongLeft: State<String?> = _wrongLeft
    private val _wrongRight = mutableStateOf<String?>(null)
    val wrongRight: State<String?> = _wrongRight

    private val _reorderSelectedWords = mutableStateListOf<String>()
    val reorderSelectedWords: List<String> = _reorderSelectedWords

    init {
        loadVocabularies()
    }

    private fun loadVocabularies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentLessonId = lessonId ?: "general"
                val vocabList = vocabularyRepository.getVocabulariesByLesson(currentLessonId)
                
                if (vocabList.isEmpty()) {
                    _error.value = "No vocabulary data found"
                    return@launch
                }

                _vocabularies.value = vocabList.shuffled()
                generateQuestions()
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateQuestions() {
        val vocabs = _vocabularies.value
        if (vocabs.isEmpty()) return

        val generatedQuestions = mutableListOf<PracticeQuestion>()

        vocabs.forEach { vocab ->
            val types = mutableListOf(
                PracticeType.EN_TO_VI, 
                PracticeType.VI_TO_EN, 
                PracticeType.AUDIO_TO_VI,
                PracticeType.FILL_BLANK
            )
            
            if (vocab.exampleSentence.isNotBlank() && vocab.exampleSentence.split(" ").size > 2) {
                types.add(PracticeType.SENTENCE_REORDER)
            }

            val type = types.random()
            val options = mutableListOf<String>()
            val distractors = vocabs.filter { it.id != vocab.id }.shuffled().take(3)

            when (type) {
                PracticeType.EN_TO_VI -> {
                    options.add(vocab.meaning)
                    options.addAll(distractors.map { it.meaning })
                    generatedQuestions.add(PracticeQuestion(vocab, type, options.shuffled(), vocab.meaning))
                }
                PracticeType.VI_TO_EN -> {
                    options.add(vocab.word)
                    options.addAll(distractors.map { it.word })
                    generatedQuestions.add(PracticeQuestion(vocab, type, options.shuffled(), vocab.word))
                }
                PracticeType.AUDIO_TO_VI -> {
                    options.add(vocab.meaning)
                    options.addAll(distractors.map { it.meaning })
                    generatedQuestions.add(PracticeQuestion(vocab, type, options.shuffled(), vocab.meaning))
                }
                PracticeType.FILL_BLANK -> {
                    generatedQuestions.add(PracticeQuestion(vocab, type, correctAnswer = vocab.word))
                }
                PracticeType.SENTENCE_REORDER -> {
                    val words = vocab.exampleSentence.trim().removeSuffix(".").split(" ")
                    generatedQuestions.add(PracticeQuestion(
                        vocab, 
                        type, 
                        correctAnswer = vocab.exampleSentence.trim().removeSuffix("."),
                        shuffledWords = words.shuffled()
                    ))
                }
                else -> {}
            }
        }

        if (vocabs.size >= 3) {
            val matchingVocabs = vocabs.take(minOf(vocabs.size, 5))
            generatedQuestions.add(PracticeQuestion(Vocabulary(), PracticeType.MATCHING, matchingPairs = matchingVocabs.map { it.word to it.meaning }))
        }

        _questions.value = generatedQuestions.shuffled()
    }

    fun onSelectAnswer(answer: String) {
        if (!_isAnswered.value) {
            _userAnswer.value = answer
        }
    }

    fun onReorderWordClick(word: String, isSelected: Boolean) {
        if (_isAnswered.value) return
        if (isSelected) {
            _reorderSelectedWords.remove(word)
        } else {
            _reorderSelectedWords.add(word)
        }
        _userAnswer.value = _reorderSelectedWords.joinToString(" ")
    }

    fun checkAnswer() {
        if (_isAnswered.value) return
        
        val currentQuestion = _questions.value.getOrNull(_currentIndex.value) ?: return
        val correct = when (currentQuestion.type) {
            PracticeType.FILL_BLANK -> _userAnswer.value.trim().equals(currentQuestion.correctAnswer, ignoreCase = true)
            PracticeType.SENTENCE_REORDER -> _userAnswer.value.trim() == currentQuestion.correctAnswer.trim()
            else -> _userAnswer.value == currentQuestion.correctAnswer
        }

        _isCorrect.value = correct
        _isAnswered.value = true
        if (correct) {
            _correctCount.value++
        } else {
            _wrongQuestions.add(currentQuestion)
        }
    }

    fun onMatchingSelect(item: String, isLeft: Boolean) {
        if (_isAnswered.value || _wrongLeft.value != null) return
        if (isLeft) _selectedLeft.value = item else _selectedRight.value = item
        val left = _selectedLeft.value
        val right = _selectedRight.value
        if (left != null && right != null) {
            val currentQuestion = _questions.value[_currentIndex.value]
            val pair = currentQuestion.matchingPairs.find { it.first == left && it.second == right }
            if (pair != null) {
                _matchedPairs.add(left)
                _matchedPairs.add(right)
                _selectedLeft.value = null
                _selectedRight.value = null
                if (_matchedPairs.size == currentQuestion.matchingPairs.size * 2) {
                    _isCorrect.value = true
                    _isAnswered.value = true
                    _correctCount.value++
                }
            } else {
                _wrongLeft.value = left
                _wrongRight.value = right
                _selectedLeft.value = null
                _selectedRight.value = null
                viewModelScope.launch {
                    delay(500)
                    _wrongLeft.value = null
                    _wrongRight.value = null
                }
                if (!_wrongQuestions.contains(currentQuestion)) {
                    _wrongQuestions.add(currentQuestion)
                }
            }
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value++
            resetQuestionState()
        } else {
            // Khi kết thúc bài, thực hiện lưu đồng bộ TRƯỚC khi hiện màn hình kết quả
            saveResultAndFinish()
        }
    }

    private fun resetQuestionState() {
        _isAnswered.value = false
        _isCorrect.value = false
        _userAnswer.value = ""
        _selectedLeft.value = null
        _selectedRight.value = null
        _wrongLeft.value = null
        _wrongRight.value = null
        _matchedPairs.clear()
        _reorderSelectedWords.clear()
    }

    private fun saveResultAndFinish() {
        val userId = auth.currentUser?.uid ?: return
        val total = _questions.value.size
        if (total == 0) return
        val score = (_correctCount.value * 100) / total

        viewModelScope.launch {
            _isLoading.value = true // Hiện loading để đảm bảo việc lưu hoàn tất
            try {
                // 1. Cập nhật bảng phân tích tổng quát (user_analytics)
                quizRepository.syncUserAnalytics(userId, score, _correctCount.value)

                // 2. Lưu lịch sử làm bài chi tiết (study_sessions)
                quizRepository.logStudySession(userId, "practice", score, _correctCount.value, total)

                // 3. Đồng bộ với bảng quiz_results để ProgressScreen lấy được "Điểm lần cuối"
                quizRepository.saveQuizResult(QuizResult(
                    userId = userId,
                    lessonId = lessonId ?: "practice",
                    type = "practice",
                    score = score,
                    correctCount = _correctCount.value,
                    totalQuestions = total
                ))

                // 4. Cập nhật mức độ thuộc từ (word_mastery)
                _questions.value.forEach { question ->
                    val vocabId = question.vocabulary.id
                    if (!vocabId.isNullOrEmpty()) {
                        val isCorrect = !_wrongQuestions.any { it.vocabulary.id == vocabId }
                        quizRepository.updateWordMastery(userId, vocabId, isCorrect)
                    }
                }
                
                Log.d("PRACTICE_DEBUG", "Save Success")
            } catch (e: Exception) {
                Log.e("PRACTICE_DEBUG", "Save Error: ${e.message}")
            } finally {
                _isLoading.value = false
                _isFinished.value = true // Hiện màn hình kết quả SAU KHI lưu xong
            }
        }
    }

    fun retry() {
        _currentIndex.value = 0
        _correctCount.value = 0
        _isFinished.value = false
        _wrongQuestions.clear()
        resetQuestionState()
        loadVocabularies()
    }

    fun retryMistakes() {
        val mistakes = _wrongQuestions.toList().shuffled()
        _questions.value = mistakes
        _wrongQuestions.clear()
        _currentIndex.value = 0
        _correctCount.value = 0
        _isFinished.value = false
        resetQuestionState()
    }
}
