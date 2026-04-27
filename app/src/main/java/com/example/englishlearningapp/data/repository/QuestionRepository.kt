package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.Question
import com.example.englishlearningapp.data.model.QuestionType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface QuestionRepository {
    suspend fun getQuestions(): List<Question>
    suspend fun getQuestionsByLesson(lessonId: String): List<Question>
    suspend fun getQuestionsByTopic(topicId: String): List<Question> // Thêm hàm này
}

@Singleton
class FirebaseQuestionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuestionRepository {

    override suspend fun getQuestions(): List<Question> {
        return try {
            val result = firestore.collection("questions")
                .get()
                .await()
            
            val questions = result.documents.mapNotNull { doc ->
                try {
                    val q = doc.toObject(Question::class.java)
                    q?.id = doc.id
                    q
                } catch (e: Exception) {
                    Log.e("FIREBASE", "Error parsing question ${doc.id}: ${e.message}")
                    null
                }
            }
            
            Log.d("FIREBASE", "Loaded total questions: ${questions.size}")
            questions.filter { it.status == "active" }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error fetching questions: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getQuestionsByLesson(lessonId: String): List<Question> {
        return try {
            Log.d("FIREBASE", "Fetching questions for lessonId: $lessonId")
            val result = firestore.collection("questions")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()
            
            val questions = result.documents.mapNotNull { doc ->
                try {
                    val q = doc.toObject(Question::class.java)
                    q?.id = doc.id
                    q
                } catch (e: Exception) {
                    Log.e("FIREBASE", "Error parsing question ${doc.id}: ${e.message}")
                    null
                }
            }
            
            Log.d("FIREBASE", "Loaded questions for lesson $lessonId: ${questions.size}")
            
            if (questions.isEmpty() && lessonId == "general") {
                return getQuestions()
            }
            
            questions.filter { it.status == "active" }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error fetching questions by lesson: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getQuestionsByTopic(topicId: String): List<Question> {
        return try {
            Log.d("FIREBASE", "Fetching questions for topicId: $topicId")
            val result = firestore.collection("questions")
                .whereEqualTo("topicId", topicId)
                .get()
                .await()

            val questions = result.documents.mapNotNull { doc ->
                try {
                    val q = doc.toObject(Question::class.java)
                    q?.id = doc.id
                    q
                } catch (e: Exception) {
                    null
                }
            }

            Log.d("FIREBASE", "Loaded questions for topic $topicId: ${questions.size}")
            questions.filter { it.status == "active" }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error fetching questions by topic: ${e.message}")
            emptyList()
        }
    }
}
