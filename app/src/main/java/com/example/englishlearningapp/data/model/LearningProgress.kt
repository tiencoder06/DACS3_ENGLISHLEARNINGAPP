package com.example.englishlearningapp.data.model

data class LearningProgress(
    val userId: String = "",
    val lessonId: String = "",
    val learnedWords: List<String> = emptyList(), // Danh sách vocabId đã học
    val completionPercent: Int = 0,
    val lastQuizScore: Int = 0,
    val status: String = "not_started" // not_started, in_progress, completed
)
