package com.example.englishlearningapp.di

import com.example.englishlearningapp.data.repository.LessonRepository
import com.example.englishlearningapp.data.repository.LessonRepositoryImpl
import com.example.englishlearningapp.data.repository.TopicRepository
import com.example.englishlearningapp.data.repository.TopicRepositoryImpl
import com.example.englishlearningapp.data.repository.VocabularyRepository
import com.example.englishlearningapp.data.repository.VocabularyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        lessonRepositoryImpl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    @Singleton
    abstract fun bindTopicRepository(
        topicRepositoryImpl: TopicRepositoryImpl
    ): TopicRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(
        vocabularyRepositoryImpl: VocabularyRepositoryImpl
    ): VocabularyRepository
}
