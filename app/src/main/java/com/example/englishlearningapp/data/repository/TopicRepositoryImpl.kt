package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TopicRepository {

    override suspend fun getTopics(): List<Topic> {
        return try {
            Log.d("Debug_Topic", "Firestore query starting: status == active, order ASC")
            val result = firestore.collection("topics")
                .whereEqualTo("status", "active")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val topics = result.toObjects(Topic::class.java)
            Log.d("Debug_Topic", "Fetch success: ${topics.size} topics found")
            topics
        } catch (e: Exception) {
            Log.e("Debug_Topic", "Fetch failed: ${e.message}")
            throw e // Cần ném lỗi để ViewModel xử lý TopicUiState.Error
        }
    }
}
