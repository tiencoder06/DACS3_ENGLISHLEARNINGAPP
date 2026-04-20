package com.example.englishlearningapp.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val avatarUrl: String? = null,
    val level: String = "Người mới bắt đầu",
    val streakDays: Int = 0,
    val wordsLearned: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
