package com.example.englishlearningapp.data.model

import com.google.firebase.Timestamp

data class QuizResult(
    var userId: String = "",
    var lessonId: String = "",
    var type: String = "quiz", // "quiz" hoặc "practice"
    var score: Int = 0,
    var correctCount: Int = 0,
    var totalQuestions: Int = 0,
    var submittedAt: Timestamp = Timestamp.now()
)