package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.FavoriteWord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun toggleFavorite(userId: String, vocabId: String, isFavorite: Boolean) {
        val docId = "${userId}_${vocabId}"
        val ref = firestore.collection("favorite_words").document(docId)

        try {
            if (isFavorite) {
                val favorite = FavoriteWord(
                    id = docId,
                    userId = userId,
                    vocabId = vocabId,
                    createdAt = System.currentTimeMillis()
                )
                ref.set(favorite).await()
            } else {
                ref.delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun isFavorite(userId: String, vocabId: String): Boolean {
        return try {
            val docId = "${userId}_${vocabId}"
            val doc = firestore.collection("favorite_words").document(docId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}
