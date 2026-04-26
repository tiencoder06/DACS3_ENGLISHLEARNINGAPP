package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.DifficultWord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DifficultWordsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Lưu từ khó. Nếu đã tồn tại thì giữ nguyên wrongCount (việc tăng wrongCount do Người 3 xử lý ở Quiz).
     * Android App cho phép user chủ động bấm "Lưu từ khó".
     */
    suspend fun addDifficultWord(userId: String, vocabId: String) {
        val docId = "${userId}_${vocabId}"
        val ref = firestore.collection("difficult_words").document(docId)
        
        try {
            val doc = ref.get().await()
            if (!doc.exists()) {
                val difficultWord = DifficultWord(
                    id = docId,
                    userId = userId,
                    vocabId = vocabId,
                    wrongCount = 0,
                    updatedAt = System.currentTimeMillis()
                )
                ref.set(difficultWord).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
