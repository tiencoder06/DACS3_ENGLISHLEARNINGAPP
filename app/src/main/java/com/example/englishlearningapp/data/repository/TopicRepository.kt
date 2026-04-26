package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.Topic

interface TopicRepository {
    suspend fun getTopics(): List<Topic>
}
