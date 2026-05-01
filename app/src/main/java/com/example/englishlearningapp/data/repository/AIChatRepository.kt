package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.AIMessage
import kotlinx.coroutines.flow.Flow

interface AIChatRepository {
    fun getChatMessages(userId: String): Flow<Result<List<AIMessage>>>
    suspend fun saveMessage(userId: String, message: AIMessage): Result<Unit>
}
