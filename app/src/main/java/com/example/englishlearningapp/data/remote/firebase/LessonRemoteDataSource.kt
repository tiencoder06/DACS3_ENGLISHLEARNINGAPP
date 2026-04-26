package com.example.englishlearningapp.data.remote.firebase

import com.example.englishlearningapp.data.model.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getLessonsByTopic(topicId: String): List<Lesson> {
        return try {
            firestore.collection(FirestoreCollections.LESSONS)
                .whereEqualTo("topicId", topicId)
                .whereEqualTo("status", "active")
                .get()
                .await()
                .toObjects(Lesson::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
