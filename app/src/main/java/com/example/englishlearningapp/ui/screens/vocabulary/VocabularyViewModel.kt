package com.example.englishlearningapp.ui.screens.vocabulary

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.data.repository.AuthRepository
import com.example.englishlearningapp.data.repository.ProgressRepository
import com.example.englishlearningapp.data.repository.VocabularyRepository
import com.example.englishlearningapp.utils.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val authRepository: AuthRepository,
    private val progressRepository: ProgressRepository,
    private val ttsHelper: TextToSpeechHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _vocabularies = MutableStateFlow<List<Vocabulary>>(emptyList())
    val vocabularies: StateFlow<List<Vocabulary>> = _vocabularies

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _favoriteStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteStatus: StateFlow<Map<String, Boolean>> = _favoriteStatus

    private val _difficultStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val difficultStatus: StateFlow<Map<String, Boolean>> = _difficultStatus

    private val _learnedWords = MutableStateFlow<Set<String>>(emptySet())
    val learnedWords: StateFlow<Set<String>> = _learnedWords

    private var mediaPlayer: MediaPlayer? = null
    private var currentLessonId: String? = null

    fun loadVocabularies(lessonId: String) {
        currentLessonId = lessonId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = authRepository.getCurrentUser()?.uid
                
                // Fetch data in parallel
                val vocabJob = async { vocabularyRepository.getVocabulariesByLesson(lessonId) }
                val favoriteJob = async { currentUserId?.let { vocabularyRepository.getFavoriteVocabIds(it) } ?: emptySet() }
                val difficultJob = async { currentUserId?.let { vocabularyRepository.getDifficultVocabIds(it) } ?: emptySet() }
                val progressJob = async { 
                    if (currentUserId != null) {
                        progressRepository.getProgress(currentUserId, lessonId)
                    } else null
                }

                val list = vocabJob.await()
                val favoriteSet = favoriteJob.await()
                val difficultSet = difficultJob.await()
                val progress = progressJob.await()

                _vocabularies.value = list
                
                // Map initial status for UI
                _favoriteStatus.value = list.associate { it.vocabId to favoriteSet.contains(it.vocabId) }
                _difficultStatus.value = list.associate { it.vocabId to difficultSet.contains(it.vocabId) }
                _learnedWords.value = progress?.learnedWords?.toSet() ?: emptySet()
                
                Log.d("Debug_Data", "Loaded data for lesson: $lessonId")
            } catch (e: Exception) {
                Log.e("Debug_Data", "Error loading data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playVocabularySound(vocabulary: Vocabulary) {
        viewModelScope.launch {
            when {
                vocabulary.audioUrl.isNotEmpty() -> {
                    Log.d("Debug_Data", "Playing online audio: ${vocabulary.audioUrl}")
                    playOnlineAudio(vocabulary.audioUrl) {
                        speakWithTTS(vocabulary.audioText.ifEmpty { vocabulary.word })
                    }
                }
                vocabulary.audioText.isNotEmpty() -> {
                    speakWithTTS(vocabulary.audioText)
                }
                else -> {
                    speakWithTTS(vocabulary.word)
                }
            }
        }
    }

    private fun playOnlineAudio(url: String, onFallback: () -> Unit) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { start() }
                setOnErrorListener { _, what, extra ->
                    Log.e("Debug_Data", "MediaPlayer Error: what=$what extra=$extra")
                    onFallback()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("Debug_Data", "MediaPlayer Exception: ${e.message}")
            onFallback()
        }
    }

    private fun speakWithTTS(text: String) {
        Log.d("Debug_Data", "Speaking with TTS: $text")
        ttsHelper.speak(text)
    }

    fun toggleFavorite(vocabId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        val currentStatus = _favoriteStatus.value[vocabId] ?: false
        val newStatus = !currentStatus
        
        // Optimistic UI update
        _favoriteStatus.value = _favoriteStatus.value + (vocabId to newStatus)

        viewModelScope.launch {
            try {
                vocabularyRepository.toggleFavorite(currentUserId, vocabId, newStatus)
            } catch (e: Exception) {
                Log.e("Debug_Data", "Error toggling favorite: ${e.message}")
                // Rollback on error
                _favoriteStatus.value = _favoriteStatus.value + (vocabId to currentStatus)
            }
        }
    }

    fun toggleDifficult(vocabId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        val currentStatus = _difficultStatus.value[vocabId] ?: false
        val newStatus = !currentStatus

        // Optimistic UI update
        _difficultStatus.value = _difficultStatus.value + (vocabId to newStatus)

        viewModelScope.launch {
            try {
                vocabularyRepository.toggleDifficultWord(currentUserId, vocabId, newStatus)
                Log.d("Debug_Data", "Toggle difficult success: $vocabId -> $newStatus")
            } catch (e: Exception) {
                Log.e("Debug_Data", "Error toggling difficult: ${e.message}")
                // Rollback on error
                _difficultStatus.value = _difficultStatus.value + (vocabId to currentStatus)
            }
        }
    }

    // Keep for backward compatibility if needed by other components
    fun saveAsDifficult(vocabId: String) = toggleDifficult(vocabId)

    fun markAsLearned(vocabId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid
        if (currentUserId == null) {
            Log.e("Debug_Data", "Cannot mark learned: User ID is null")
            return
        }
        val lessonId = currentLessonId ?: return
        
        Log.d("Debug_Data", "Marking as learned: $vocabId for user: $currentUserId")
        viewModelScope.launch {
            try {
                progressRepository.markWordAsLearned(currentUserId, lessonId, vocabId)
                _learnedWords.value = _learnedWords.value + vocabId
                Log.d("Debug_Data", "Successfully marked $vocabId as learned")
            } catch (e: Exception) {
                Log.e("Debug_Data", "Error marking learned: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
