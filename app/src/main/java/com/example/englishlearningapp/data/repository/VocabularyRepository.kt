package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.Vocabulary
import com.example.englishlearningapp.data.remote.firebase.VocabularyRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepository @Inject constructor(
    private val remoteDataSource: VocabularyRemoteDataSource
) {
    suspend fun getVocabulariesByLesson(lessonId: String): List<Vocabulary> {
        return remoteDataSource.getVocabulariesByLesson(lessonId)
    }

    suspend fun getAllActiveVocabularies(): List<Vocabulary> {
        return remoteDataSource.getAllActiveVocabularies()
    }

    suspend fun getVocabulariesByIds(ids: List<String>): List<Vocabulary> {
        return remoteDataSource.getVocabulariesByIds(ids)
    }
}
