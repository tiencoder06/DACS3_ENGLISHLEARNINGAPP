package com.example.englishlearningapp.data.model

data class DifficultWord(
    val id: String = "", // userId_vocabId
    val userId: String = "",
    val vocabId: String = "",
    val wrongCount: Int = 0,
    val updatedAt: Long = 0L
)
