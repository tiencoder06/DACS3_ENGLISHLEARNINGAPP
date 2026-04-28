package com.example.englishlearningapp.data.model

import com.google.firebase.firestore.PropertyName

enum class QuestionType {
    @PropertyName("multiple_choice")
    MULTIPLE_CHOICE,
    @PropertyName("fill_blank")
    FILL_BLANK,
    @PropertyName("listen_choose")
    AUDIO,
    @PropertyName("matching")
    MATCHING
}

data class Question(
    @get:PropertyName("questionId")
    @set:PropertyName("questionId")
    var id: String = "",

    @get:PropertyName("lessonId")
    @set:PropertyName("lessonId")
    var lessonId: String = "",

    @get:PropertyName("vocabId")
    @set:PropertyName("vocabId")
    var vocabId: String = "",

    @get:PropertyName("questionType")
    @set:PropertyName("questionType")
    var type: QuestionType = QuestionType.MULTIPLE_CHOICE,

    @get:PropertyName("questionText")
    @set:PropertyName("questionText")
    var question: String = "",

    var options: List<String>? = emptyList(),

    @get:PropertyName("correctAnswer")
    @set:PropertyName("correctAnswer")
    var correctAnswer: String = "",

    @get:PropertyName("explanation")
    @set:PropertyName("explanation")
    var audioText: String? = null,

    @get:PropertyName("audioUrl")
    @set:PropertyName("audioUrl")
    var audioUrl: String? = null,

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "active",

    @get:PropertyName("usage")
    @set:PropertyName("usage")
    var usage: String = "both", // "quiz", "practice", "both"
    
    // For Matching questions: left items and right items
    var matchingPairs: Map<String, String>? = null
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", QuestionType.MULTIPLE_CHOICE, "", emptyList(), "", null, null, "active", "both", null)
}
