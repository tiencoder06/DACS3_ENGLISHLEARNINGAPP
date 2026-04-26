package com.example.englishlearningapp.data.model

data class FavoriteWord(
    val id: String = "", // Định dạng: userId_vocabId
    val userId: String = "",
    val vocabId: String = "",
    val createdAt: Long = 0L
)
