package com.example.englishlearningapp.data.model

data class PlacementQuestion(
    val questionId: String = "",
    val section: String = "", // "vocabulary_grammar" | "listening" | "sentence_usage"
    val questionType: String = "", // "multiple_choice" | "listen_choose"
    val questionText: String = "",
    val audioText: String = "",
    val audioUrl: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val order: Int = 0,
    val level: Int = 1,
    val status: String = "active" // "active" | "inactive" | "deleted"
)
