package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.DifficultWord
import com.example.englishlearningapp.data.model.FavoriteWord
import com.example.englishlearningapp.data.model.Vocabulary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : VocabularyRepository {

    override suspend fun getVocabulariesByLesson(lessonId: String): List<Vocabulary> {
        return try {
            val snapshot = firestore.collection("vocabularies")
                .whereEqualTo("lessonId", lessonId)
                .whereEqualTo("status", "active")
                .get()
                .await()
            
            val documents = snapshot.documents
            val vocabList = mutableListOf<Vocabulary>()
            documents.forEach { doc ->
                try {
                    val vocab = doc.toObject(Vocabulary::class.java)
                    if (vocab != null) {
                        val finalVocab = if (vocab.vocabId.isEmpty()) vocab.copy(vocabId = doc.id) else vocab
                        vocabList.add(finalVocab)
                    }
                } catch (e: Exception) {
                    Log.e("VocabularyRepo", "Error mapping vocab ${doc.id}", e)
                }
            }
            vocabList
        } catch (e: Exception) {
            Log.e("VocabularyRepo", "Error fetching vocabularies", e)
            emptyList()
        }
    }

    override suspend fun toggleFavorite(userId: String, vocabId: String, isFavorite: Boolean) {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid == null) {
            Log.e("VOCAB_ACTION", "Lỗi: Không tìm thấy User ID. User chưa đăng nhập!")
            throw Exception("User not logged in")
        }

        val id = "${currentUid}_$vocabId"
        val docRef = firestore.collection("favorite_words").document(id)
        
        try {
            Log.d("VOCAB_ACTION", "Đang xử lý toggleFavorite với docId: $id")
            if (isFavorite) {
                val favorite = FavoriteWord(
                    id = id,
                    userId = currentUid,
                    vocabId = vocabId,
                    createdAt = System.currentTimeMillis()
                )
                docRef.set(favorite).await()
            } else {
                docRef.delete().await()
            }
            Log.d("VOCAB_ACTION", "Toggle favorite thành công: $vocabId -> $isFavorite")
        } catch (e: Exception) {
            Log.e("VOCAB_ACTION", "Lỗi Firestore khi toggle favorite: ${e.message}")
            throw e
        }
    }

    override suspend fun toggleDifficultWord(userId: String, vocabId: String, isDifficult: Boolean) {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid == null) {
            Log.e("VOCAB_ACTION", "Lỗi: Không tìm thấy User ID. User chưa đăng nhập!")
            throw Exception("User not logged in")
        }

        val id = "${currentUid}_$vocabId"
        val docRef = firestore.collection("difficult_words").document(id)
        
        try {
            Log.d("VOCAB_ACTION", "Đang xử lý toggleDifficult với docId: $id")
            if (isDifficult) {
                val difficultData = mapOf(
                    "id" to id,
                    "userId" to currentUid,
                    "vocabId" to vocabId,
                    "wrongCount" to FieldValue.increment(1),
                    "updatedAt" to System.currentTimeMillis()
                )
                docRef.set(difficultData, SetOptions.merge()).await()
            } else {
                docRef.delete().await()
            }
            Log.d("VOCAB_ACTION", "Toggle difficult thành công: $vocabId -> $isDifficult")
        } catch (e: Exception) {
            Log.e("VOCAB_ACTION", "Lỗi Firestore khi toggle difficult: ${e.message}")
            throw e
        }
    }

    override suspend fun getFavoriteVocabIds(userId: String): Set<String> {
        return try {
            val snapshot = firestore.collection("favorite_words")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.getString("vocabId") }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override suspend fun getDifficultVocabIds(userId: String): Set<String> {
        return try {
            val snapshot = firestore.collection("difficult_words")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.getString("vocabId") }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    override suspend fun checkIfVocabularyLearned(vocabularyId: String): Boolean {
        val currentUid = firebaseAuth.currentUser?.uid ?: return false
        val docId = "${currentUid}_$vocabularyId"
        return try {
            val doc = firestore.collection("learning_progress")
                .document(docId)
                .get()
                .await()
            doc.exists() && doc.getBoolean("isLearned") == true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markVocabularyAsLearned(vocabulary: Vocabulary) {
        val currentUid = firebaseAuth.currentUser?.uid ?: return
        val docId = "${currentUid}_${vocabulary.vocabId}"
        val data = hashMapOf(
            "userId" to currentUid,
            "vocabularyId" to vocabulary.vocabId,
            "lessonId" to vocabulary.lessonId,
            "isLearned" to true,
            "timestamp" to FieldValue.serverTimestamp()
        )
        try {
            firestore.collection("learning_progress")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
}
