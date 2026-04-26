package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LessonRepository {

    override suspend fun getLessons(topicId: String): List<Lesson> {
        return try {
            Log.d("Debug_Data", "Lesson query starting for topicId: $topicId")
            val snapshot = firestore.collection("lessons")
                .whereEqualTo("topicId", topicId)
                .whereEqualTo("status", "active")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val documents = snapshot.documents
            Log.d("Debug_Data", "Found ${documents.size} documents in Firestore")
            
            val lessons = mutableListOf<Lesson>()
            documents.forEach { doc ->
                Log.d("Debug_Data", "Raw Doc: ${doc.id} -> ${doc.data}")
                try {
                    val lesson = doc.toObject(Lesson::class.java)
                    if (lesson != null) {
                        // Ưu tiên dùng doc.id nếu lessonId trong data bị trống
                        val finalLesson = if (lesson.lessonId.isEmpty()) lesson.copy(lessonId = doc.id) else lesson
                        lessons.add(finalLesson)
                    }
                } catch (e: Exception) {
                    Log.e("Debug_Data", "Error mapping doc ${doc.id}: ${e.message}")
                }
            }
            
            Log.d("Debug_Data", "Final mapped list size: ${lessons.size}")
            lessons
        } catch (e: Exception) {
            Log.e("Debug_Data", "Firestore Error: ${e.message}")
            throw e
        }
    }
}
