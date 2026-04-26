package com.example.englishlearningapp.data.remote.firebase

import com.example.englishlearningapp.data.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Lấy danh sách Topic từ Firestore.
     * Quy tắc: Chỉ lấy các topic có status == "active" và sắp xếp theo trường "order" tăng dần.
     */
    suspend fun getAllTopics(): List<Topic> {
        return try {
            firestore.collection("topics")
                .whereEqualTo("status", "active")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(Topic::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
