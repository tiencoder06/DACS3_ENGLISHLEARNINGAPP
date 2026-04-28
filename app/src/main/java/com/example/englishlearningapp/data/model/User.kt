package com.example.englishlearningapp.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "user",
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String = "",
    val lastActiveAt: Timestamp? = null,
    val wordsLearned: Int = 0,
    val level: String = "Beginner",
    val createdAt: Long = System.currentTimeMillis(),
    
    // New Unified Progress Fields
    val totalQuizAttempts: Int = 0,
    val totalScoreSum: Int = 0,
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val difficultWords: List<String> = emptyList(),

    // Placement Test Fields
    val placementCompleted: Boolean = false,
    val placementSkipped: Boolean = false,
    val placementScore: Int = 0,
    val placementLevel: String = "Beginner",
    val placementStrongSkill: String = "",
    val placementWeakSkill: String = "",
    val recommendedStartTopicId: String = "",
    val recommendedStartLessonId: String = "",
    val placementTakenAt: Timestamp? = null
)
