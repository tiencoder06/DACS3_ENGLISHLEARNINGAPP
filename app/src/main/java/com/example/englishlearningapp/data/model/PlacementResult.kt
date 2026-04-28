package com.example.englishlearningapp.data.model

data class PlacementResult(
    val score: Int = 0,
    val level: String = "Beginner",
    val strongSkill: String = "",
    val weakSkill: String = "",
    val recommendedTopicId: String = "",
    val recommendedLessonId: String = "",
    val sectionScores: Map<String, Int> = emptyMap()
)
