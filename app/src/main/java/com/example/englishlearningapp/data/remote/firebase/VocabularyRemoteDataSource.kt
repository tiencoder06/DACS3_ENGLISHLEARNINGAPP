package com.example.englishlearningapp.data.remote.firebase

import android.util.Log
import com.example.englishlearningapp.data.model.Vocabulary
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getVocabulariesByLesson(lessonId: String): List<Vocabulary> {
        return try {
            Log.d("FIREBASE", "Fetching vocab for lessonId: $lessonId")
            val result = firestore.collection("vocabularies")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()
            
            val list = result.documents.mapNotNull { doc ->
                try {
                    val v = doc.toObject(Vocabulary::class.java)
                    v?.id = doc.id // Ensure ID is set
                    v
                } catch (e: Exception) {
                    Log.e("FIREBASE", "Error parsing vocab ${doc.id}: ${e.message}")
                    null
                }
            }
            
            Log.d("FIREBASE", "Loaded vocab for lesson $lessonId: ${list.size}")
            
            // If lesson-specific returns nothing and it's 'general', return all as fallback
            if (list.isEmpty() && lessonId == "general") {
                return getAllActiveVocabularies()
            }
            
            list.filter { it.status == "active" }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error getting vocab by lesson: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllActiveVocabularies(): List<Vocabulary> {
        return try {
            val result = firestore.collection("vocabularies")
                .get()
                .await()
            
            val list = result.documents.mapNotNull { doc ->
                val v = doc.toObject(Vocabulary::class.java)
                v?.id = doc.id
                v
            }
            Log.d("FIREBASE", "Loaded total active vocab: ${list.size}")
            list.filter { it.status == "active" }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error getting all vocab: ${e.message}")
            emptyList()
        }
    }

    suspend fun getVocabulariesByIds(ids: List<String>): List<Vocabulary> {
        if (ids.isEmpty()) return emptyList()
        return try {
            firestore.collection("vocabularies")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(Vocabulary::class.java)?.apply { id = it.id } }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
