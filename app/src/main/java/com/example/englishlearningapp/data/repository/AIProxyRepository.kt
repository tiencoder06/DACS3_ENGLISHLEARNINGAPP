package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.AIMessage
import com.example.englishlearningapp.utils.Resource

data class AIProxyRequest(
    val userMessage: String,
    val placementLevel: String?,
    val placementWeakSkill: String?,
    val recentMessages: List<AIMessage>
)

data class AIProxyResponse(
    val success: Boolean,
    val assistantMessage: String?,
    val errorMessage: String?
)

interface AIProxyRepository {
    suspend fun askWorker(
        userMessage: String,
        placementLevel: String?,
        placementWeakSkill: String?,
        recentMessages: List<AIMessage>
    ): Resource<String>
}
