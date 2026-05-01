package com.example.englishlearningapp.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AIMessage(
    val role: String = "", // "user" | "assistant"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
