package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.Lesson

interface LessonRepository {
    suspend fun getLessons(topicId: String): List<Lesson>
}
