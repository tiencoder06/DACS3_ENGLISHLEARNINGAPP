package com.example.englishlearningapp.data.model

import com.google.firebase.Timestamp

data class Topic(
    val topicId: String = "",
    val name: String = "",
    val description: String = "",
    val order: Int = 0,
    val status: String = "active",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val createdBy: String = "",
    val updatedBy: String = "",
    val masteryPercentage: Float = 0f,
    val iconType: String = "default"
)
