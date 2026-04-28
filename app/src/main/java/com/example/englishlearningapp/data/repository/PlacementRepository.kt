package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.PlacementQuestion
import com.example.englishlearningapp.data.model.PlacementResult
import kotlinx.coroutines.flow.Flow

interface PlacementRepository {
    fun getPlacementQuestions(): Flow<Result<List<PlacementQuestion>>>
    
    suspend fun calculatePlacementResult(
        questions: List<PlacementQuestion>,
        userAnswers: Map<String, String>
    ): PlacementResult
    
    suspend fun savePlacementResult(
        userId: String,
        result: PlacementResult
    ): Result<Unit>
    
    suspend fun skipPlacement(userId: String): Result<Unit>
    
    suspend fun getRecommendedStartContent(level: String): Pair<String, String>
}
