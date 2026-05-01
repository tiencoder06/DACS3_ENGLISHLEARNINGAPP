package com.example.englishlearningapp.data.model

data class AIMessage(
    val role: String, // "user" | "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
