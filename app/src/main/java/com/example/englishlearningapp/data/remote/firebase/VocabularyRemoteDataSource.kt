package com.example.englishlearningapp.data.remote.firebase

import com.example.englishlearningapp.data.model.Vocabulary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Lấy danh sách từ vựng theo lessonId.
     * Quy tắc: lessonId khớp và status == "active".
     */
    suspend fun getVocabulariesByLesson(lessonId: String): List<Vocabulary> {
        return try {
            firestore.collection("vocabularies")
                .whereEqualTo("lessonId", lessonId)
                .whereEqualTo("status", "active")
                .get()
                .await()
                .toObjects(Vocabulary::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
