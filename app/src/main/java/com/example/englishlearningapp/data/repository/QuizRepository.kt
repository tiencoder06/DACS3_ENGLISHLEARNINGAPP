package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.QuizResult
import com.example.englishlearningapp.data.model.User

interface QuizRepository {
    // --- 3 Bảng Mới (Chuẩn hóa cho đồ án) ---
    
    // 1. user_analytics: Cập nhật và lấy thông số tổng quát
    suspend fun syncUserAnalytics(userId: String, score: Int, correctCount: Int): Boolean
    suspend fun getUserAnalytics(userId: String): Map<String, Any>?

    // 2. study_sessions: Lưu lịch sử làm bài chi tiết
    suspend fun logStudySession(userId: String, type: String, score: Int, correctCount: Int, total: Int): Boolean
    suspend fun getStudySessionCount(userId: String, type: String): Int
    
    // 3. word_mastery: Theo dõi mức độ thuộc từ
    suspend fun updateWordMastery(userId: String, vocabId: String, isCorrect: Boolean): Boolean
    suspend fun trackVocabularySeen(userId: String, vocabId: String): Boolean
    suspend fun getWeakWordsCount(userId: String): Int

    // --- Chức năng Ôn lại lỗi sai (Review Mistakes) ---
    suspend fun updateDifficultWordReview(userId: String, vocabId: String, isCorrect: Boolean): Boolean

    // --- Các hàm cũ (Giữ nguyên cho đồng đội) ---
    suspend fun saveQuizResult(result: QuizResult): Boolean
    suspend fun getQuizResults(userId: String): List<QuizResult>
    suspend fun updateLearningProgress(userId: String, lessonId: String, score: Int): Boolean
    suspend fun getLearningProgress(userId: String): List<Map<String, Any>>
    suspend fun updateDifficultWords(userId: String, vocabId: String): Boolean
    suspend fun getDifficultWords(userId: String): List<Map<String, Any>>
    suspend fun incrementWordsLearned(userId: String, count: Int): Boolean
    suspend fun getUserStats(userId: String): Map<String, Any>?
    suspend fun updateStreak(userId: String): Boolean
    suspend fun getUserData(userId: String): User?
    suspend fun updateProgress(userId: String, score: Int, correctCount: Int, difficultWords: List<String>): Boolean
}
