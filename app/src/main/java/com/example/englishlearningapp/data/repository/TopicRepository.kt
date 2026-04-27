package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.Topic
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface TopicRepository {
    suspend fun getTopics(): List<Topic>
}

@Singleton
class FirebaseTopicRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : TopicRepository {

    override suspend fun getTopics(): List<Topic> {
        return try {
            val result = firestore.collection("topics")
                .get()
                .await()

            val topics = result.documents.mapNotNull { doc ->
                try {
                    val topic = doc.toObject(Topic::class.java)
                    topic?.id = doc.id
                    topic
                } catch (e: Exception) {
                    Log.e("FIREBASE", "Error parsing topic ${doc.id}: ${e.message}")
                    null
                }
            }
            topics.filter { it.status == "active" }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error fetching topics: ${e.message}")
            emptyList()
        }
    }
}
