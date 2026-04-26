package com.example.englishlearningapp.ui.screens.lesson

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishlearningapp.data.model.Lesson
import com.example.englishlearningapp.data.model.Topic
import com.example.englishlearningapp.data.repository.AuthRepository
import com.example.englishlearningapp.data.repository.LessonRepository
import com.example.englishlearningapp.data.repository.ProgressRepository
import com.example.englishlearningapp.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val topicRepository: TopicRepository,
    private val progressRepository: ProgressRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    private val _currentTopic = MutableStateFlow<Topic?>(null)
    val currentTopic: StateFlow<Topic?> = _currentTopic

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _lessonProgress = MutableStateFlow<Map<String, String>>(emptyMap())
    val lessonProgress: StateFlow<Map<String, String>> = _lessonProgress
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadData(topicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Load Topic Info
                val topics = topicRepository.getTopics()
                _currentTopic.value = topics.find { it.topicId == topicId }

                // Load Lessons
                val list = lessonRepository.getLessons(topicId)
                _lessons.value = list
                
                // Load Progress
                val currentUserId = authRepository.getCurrentUser()?.uid
                if (currentUserId != null) {
                    val progressMap = mutableMapOf<String, String>()
                    list.forEach { lesson ->
                        val progress = progressRepository.getProgress(currentUserId, lesson.lessonId)
                        progressMap[lesson.lessonId] = progress?.status ?: "not_started"
                    }
                    _lessonProgress.value = progressMap
                }
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("Debug_Data", "Error in LessonViewModel: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
