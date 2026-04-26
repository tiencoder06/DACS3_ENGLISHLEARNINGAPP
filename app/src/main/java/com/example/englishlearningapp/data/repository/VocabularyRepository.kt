package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.Vocabulary

interface VocabularyRepository {
    suspend fun getVocabulariesByLesson(lessonId: String): List<Vocabulary>
    suspend fun toggleFavorite(userId: String, vocabId: String, isFavorite: Boolean)
    suspend fun toggleDifficultWord(userId: String, vocabId: String, isDifficult: Boolean)
    suspend fun getFavoriteVocabIds(userId: String): Set<String>
    suspend fun getDifficultVocabIds(userId: String): Set<String>
    
    // "I Learned This" functionality
    suspend fun checkIfVocabularyLearned(vocabularyId: String): Boolean
    suspend fun markVocabularyAsLearned(vocabulary: Vocabulary)
}
