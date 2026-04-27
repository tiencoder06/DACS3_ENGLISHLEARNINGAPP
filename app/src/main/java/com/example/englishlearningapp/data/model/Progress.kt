package com.example.englishlearningapp.data.model

data class Progress(
    val totalQuizAttempts: Int = 0,
    val totalPracticeAttempts: Int = 0,
    val totalVocabulariesLearned: Int = 0,
    val totalAnswers: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Int = 0, // (correctAnswers / totalAnswers) * 100
    val weakWordsCount: Int = 0,
    val mistakeCount: Int = 0, // Alias or specific count for UI
    val averageScore: Int = 0,
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val streak: Int = 0,
    val evaluation: String = "Bắt đầu học ngay",
    val suggestion: String = "Hoàn thành bài kiểm tra đầu tiên để xem phân tích."
)
