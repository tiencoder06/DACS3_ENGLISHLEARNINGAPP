package com.example.englishlearningapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Lesson(
    val lessonId: String = "",
    val topicId: String = "",
    val name: String = "",
    val description: String = "",
    val order: Int = 0,
    val totalWords: Int = 0,
    val status: String = "active",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val createdBy: String = "",
    val updatedBy: String = "",
    val content: String = ""
)
