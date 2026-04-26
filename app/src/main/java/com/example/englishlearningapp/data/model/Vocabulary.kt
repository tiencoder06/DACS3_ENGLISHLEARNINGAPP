package com.example.englishlearningapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Vocabulary(
    val vocabId: String = "",
    val lessonId: String = "",
    val word: String = "",
    val meaning: String = "",
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val exampleSentence: String = "", // Schema chính
    val example: String = "",         // Backup cho dữ liệu cũ/sai
    val audioText: String = "",
    val audioUrl: String = "",
    val pronunciationSource: String = "",
    val status: String = "active",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val createdBy: String = "",
    val updatedBy: String = ""
) {
    // Helper để lấy ví dụ bất kể tên trường nào
    fun getDisplayExample(): String {
        return exampleSentence.ifEmpty { example }
    }
}
