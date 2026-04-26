package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.LearningProgress
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Đánh dấu một từ vựng là đã học trong một bài học.
     * Cập nhật danh sách learnedWords và chuyển status sang in_progress nếu đang là not_started.
     */
    suspend fun markWordAsLearned(userId: String, lessonId: String, vocabId: String) {
        val docId = "${userId}_${lessonId}"
        val ref = firestore.collection("learning_progress").document(docId)

        try {
            val doc = ref.get().await()
            if (doc.exists()) {
                ref.update(
                    "learnedWords", FieldValue.arrayUnion(vocabId),
                    "status", "in_progress"
                ).await()
            } else {
                // Tạo mới nếu chưa có tiến độ cho lesson này
                val progress = LearningProgress(
                    userId = userId,
                    lessonId = lessonId,
                    learnedWords = listOf(vocabId),
                    status = "in_progress"
                )
                ref.set(progress).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getProgress(userId: String, lessonId: String): LearningProgress? {
        return try {
            val docId = "${userId}_${lessonId}"
            firestore.collection("learning_progress").document(docId)
                .get()
                .await()
                .toObject(LearningProgress::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
