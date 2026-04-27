package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.QuizResult
import com.example.englishlearningapp.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseQuizRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : QuizRepository {

    override suspend fun syncUserAnalytics(userId: String, score: Int, correctCount: Int): Boolean {
        return try {
            val ref = firestore.collection("users").document(userId)
            val doc = ref.get().await()
            
            val currentBest = if (doc.exists()) doc.getLong("bestScore") ?: 0L else 0L
            val newBest = if (score > currentBest) score.toLong() else currentBest

            val data = hashMapOf(
                "totalAttempts" to FieldValue.increment(1),
                "totalScore" to FieldValue.increment(score.toLong()),
                "totalCorrect" to FieldValue.increment(correctCount.toLong()),
                "bestScore" to newBest,
                "lastScore" to score.toLong(),
                "lastActive" to FieldValue.serverTimestamp(),
                "lastStudyDate" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            ref.set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) { 
            Log.e("REPO_DEBUG", "Error: ${e.message}")
            false 
        }
    }

    override suspend fun getUserAnalytics(userId: String): Map<String, Any>? {
        return try {
            firestore.collection("users").document(userId).get().await().data
        } catch (e: Exception) { null }
    }

    override suspend fun logStudySession(userId: String, type: String, score: Int, correctCount: Int, total: Int): Boolean {
        return try {
            val session = hashMapOf(
                "userId" to userId,
                "sessionType" to type,
                "score" to score,
                "correctCount" to correctCount,
                "totalQuestions" to total,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("study_sessions").add(session)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getStudySessionCount(userId: String, type: String): Int {
        return try {
            firestore.collection("study_sessions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("sessionType", type)
                .get()
                .await()
                .size()
        } catch (e: Exception) { 0 }
    }

    override suspend fun updateWordMastery(userId: String, vocabId: String, isCorrect: Boolean): Boolean {
        return try {
            val docId = "${userId}_${vocabId}"
            val ref = firestore.collection("word_mastery").document(docId)
            val updates = hashMapOf(
                "userId" to userId,
                "vocabId" to vocabId,
                "lastTested" to FieldValue.serverTimestamp()
            )
            if (isCorrect) {
                updates["totalCorrect"] = FieldValue.increment(1)
            } else {
                updates["totalWrong"] = FieldValue.increment(1)
            }
            ref.set(updates, SetOptions.merge())
            true
        } catch (e: Exception) { false }
    }

    override suspend fun trackVocabularySeen(userId: String, vocabId: String): Boolean {
        return try {
            val docId = "${userId}_${vocabId}"
            val ref = firestore.collection("word_mastery").document(docId)
            val updates = hashMapOf(
                "userId" to userId,
                "vocabId" to vocabId,
                "seenCount" to FieldValue.increment(1),
                "lastSeen" to FieldValue.serverTimestamp()
            )
            ref.set(updates, SetOptions.merge())
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getWeakWordsCount(userId: String): Int {
        return try {
            firestore.collection("word_mastery")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("totalWrong", 0)
                .get()
                .await()
                .size()
        } catch (e: Exception) { 0 }
    }

    override suspend fun updateDifficultWordReview(userId: String, vocabId: String, isCorrect: Boolean): Boolean {
        return try {
            val docId = "${userId}_${vocabId}"
            val ref = firestore.collection("difficult_words").document(docId)
            
            if (isCorrect) {
                val doc = ref.get().await()
                if (doc.exists()) {
                    val wrongCount = doc.getLong("wrongCount") ?: 0L
                    if (wrongCount <= 1) {
                        ref.delete().await()
                    } else {
                        ref.update("wrongCount", FieldValue.increment(-1)).await()
                    }
                }
            } else {
                ref.set(hashMapOf(
                    "userId" to userId,
                    "vocabId" to vocabId,
                    "wrongCount" to FieldValue.increment(1),
                    "lastWrong" to FieldValue.serverTimestamp()
                ), SetOptions.merge()).await()
            }
            true
        } catch (e: Exception) {
            Log.e("REPO_DEBUG", "Error updating difficult word: ${e.message}")
            false
        }
    }

    override suspend fun saveQuizResult(result: QuizResult): Boolean {
        return try {
            val resultMap = hashMapOf(
                "userId" to result.userId,
                "lessonId" to result.lessonId,
                "score" to result.score,
                "correctCount" to result.correctCount,
                "totalQuestions" to result.totalQuestions,
                "submittedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("quiz_results").add(resultMap)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getQuizResults(userId: String): List<QuizResult> {
        return try {
            val snapshot = firestore.collection("quiz_results")
                .whereEqualTo("userId", userId)
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                QuizResult(
                    userId = doc.getString("userId") ?: "",
                    lessonId = doc.getString("lessonId") ?: "general",
                    type = "quiz",
                    score = doc.getLong("score")?.toInt() ?: 0,
                    correctCount = doc.getLong("correctCount")?.toInt() ?: 0,
                    totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0,
                    submittedAt = doc.getTimestamp("submittedAt") ?: Timestamp.now()
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun updateLearningProgress(userId: String, lessonId: String, score: Int): Boolean {
        return try {
            val ref = firestore.collection("learning_progress").document("${userId}_${lessonId}")
            val data = hashMapOf(
                "userId" to userId,
                "lessonId" to lessonId,
                "completionPercent" to score,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
            ref.set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateDifficultWords(userId: String, vocabId: String): Boolean {
        return try {
            val docId = "${userId}_${vocabId}"
            val ref = firestore.collection("difficult_words").document(docId)
            val data = hashMapOf(
                "userId" to userId,
                "vocabId" to vocabId,
                "wrongCount" to FieldValue.increment(1),
                "lastWrong" to FieldValue.serverTimestamp()
            )
            ref.set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getUserData(userId: String): User? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                User(
                    uid = doc.getString("uid") ?: userId,
                    email = doc.getString("email") ?: "",
                    fullName = doc.getString("fullName") ?: "",
                    role = doc.getString("role") ?: "user",
                    streakDays = doc.getLong("streakDays")?.toInt() ?: 0,
                    wordsLearned = doc.getLong("wordsLearned")?.toInt() ?: 0,
                    level = doc.getString("level") ?: "Beginner",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) { null }
    }

    override suspend fun updateProgress(userId: String, score: Int, correctCount: Int, difficultWords: List<String>): Boolean { 
        return true 
    }
    
    override suspend fun getLearningProgress(userId: String): List<Map<String, Any>> { return emptyList() }
    
    override suspend fun getDifficultWords(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("difficult_words")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.map { it.data ?: emptyMap<String, Any>() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun incrementWordsLearned(userId: String, count: Int): Boolean {
        return try {
            firestore.collection("users").document(userId).update("wordsLearned", FieldValue.increment(count.toLong())).await()
            true
        } catch (e: Exception) { false }
    }
    override suspend fun getUserStats(userId: String): Map<String, Any>? {
        return try { firestore.collection("users").document(userId).get().await().data } catch (e: Exception) { null }
    }
    override suspend fun updateStreak(userId: String): Boolean {
        return try {
            firestore.collection("users").document(userId).update("streakDays", FieldValue.increment(1)).await()
            true
        } catch (e: Exception) { false }
    }
}
