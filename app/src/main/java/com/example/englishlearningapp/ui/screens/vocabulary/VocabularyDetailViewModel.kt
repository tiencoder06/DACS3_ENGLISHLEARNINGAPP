package com.example.englishlearningapp.ui.screens.vocabulary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.data.repository.AuthRepository
import com.example.englishlearningapp.data.repository.UserRepository
import com.example.englishlearningapp.data.repository.VocabularyRepository
import com.example.englishlearningapp.utils.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyDetailViewModel @Inject constructor(
    private val repository: VocabularyRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val ttsHelper: TextToSpeechHelper
) : ViewModel() {

    private val _vocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabularies: StateFlow<List<Vocabulary>> = _vocabularies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLearned = MutableStateFlow(false)
    val isLearned: StateFlow<Boolean> = _isLearned.asStateFlow()

    private val _favoriteStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteStatus: StateFlow<Map<String, Boolean>> = _favoriteStatus.asStateFlow()

    private val _difficultStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val difficultStatus: StateFlow<Map<String, Boolean>> = _difficultStatus.asStateFlow()

    fun loadVocabularies(lessonId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = authRepository.getCurrentUser()?.uid
                val list = repository.getVocabulariesByLesson(lessonId)
                _vocabularies.value = list
                
                if (currentUserId != null) {
                    val favorites = repository.getFavoriteVocabIds(currentUserId)
                    val difficults = repository.getDifficultVocabIds(currentUserId)
                    _favoriteStatus.value = list.associate { it.vocabId to favorites.contains(it.vocabId) }
                    _difficultStatus.value = list.associate { it.vocabId to difficults.contains(it.vocabId) }
                }
            } catch (e: Exception) {
                Log.e("VocabDetailVM", "Error loading vocabularies", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkIfVocabularyLearned(vocabularyId: String) {
        viewModelScope.launch {
            try {
                _isLearned.value = repository.checkIfVocabularyLearned(vocabularyId)
            } catch (e: Exception) {
                _isLearned.value = false
            }
        }
    }

    fun onMarkLearnedClick(vocabulary: Vocabulary) {
        viewModelScope.launch {
            try {
                repository.markVocabularyAsLearned(vocabulary)
                _isLearned.value = true
                // Record study activity for streak logic
                userRepository.recordStudyActivity()
            } catch (e: Exception) {
                Log.e("VocabDetailVM", "Error marking as learned", e)
            }
        }
    }

    fun toggleFavorite(vocabId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        val currentStatus = _favoriteStatus.value[vocabId] ?: false
        val newStatus = !currentStatus
        viewModelScope.launch {
            try {
                repository.toggleFavorite(currentUserId, vocabId, newStatus)
                _favoriteStatus.value = _favoriteStatus.value + (vocabId to newStatus)
            } catch (e: Exception) {
                Log.e("VocabDetailVM", "Error toggling favorite", e)
            }
        }
    }

    fun toggleDifficult(vocabId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        val currentStatus = _difficultStatus.value[vocabId] ?: false
        val newStatus = !currentStatus
        viewModelScope.launch {
            try {
                repository.toggleDifficultWord(currentUserId, vocabId, newStatus)
                _difficultStatus.value = _difficultStatus.value + (vocabId to newStatus)
            } catch (e: Exception) {
                Log.e("VocabDetailVM", "Error toggling difficult", e)
            }
        }
    }

    fun playSound(vocabulary: Vocabulary) {
        ttsHelper.speak(vocabulary.audioText.ifEmpty { vocabulary.word })
    }
}
