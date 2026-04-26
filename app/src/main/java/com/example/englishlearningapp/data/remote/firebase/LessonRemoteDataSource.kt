package com.example.englishlearningapp.data.remote.firebase

import com.example.englishlearningapp.data.model.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Lấy danh sách Lesson theo topicId.
     * Quy tắc: topicId khớp, status == "active", sắp xếp theo order tăng dần.
     */
    suspend fun getLessonsByTopic(topicId: String): List<Lesson> {
        return try {
            firestore.collection("lessons")
                .whereEqualTo("topicId", topicId)
                .whereEqualTo("status", "active")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(Lesson::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
