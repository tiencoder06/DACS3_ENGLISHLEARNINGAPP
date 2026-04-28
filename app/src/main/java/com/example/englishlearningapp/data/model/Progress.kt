package com.example.englishlearningapp.data.model

data class Progress(
    val totalQuizAttempts: Int = 0,
    val totalPracticeAttempts: Int = 0,
    val totalVocabulariesLearned: Int = 0,
    val averageScore: Int = 0,
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val mistakeCount: Int = 0,
    val evaluation: String = "Need Improvement",
    val suggestion: String = ""
)