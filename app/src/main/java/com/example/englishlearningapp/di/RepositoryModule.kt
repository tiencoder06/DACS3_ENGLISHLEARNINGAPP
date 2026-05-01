package com.example.englishlearningapp.di

import com.example.englishlearningapp.data.repository.*
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
        impl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    @Singleton
    abstract fun bindTopicRepository(
        impl: TopicRepositoryImpl
    ): TopicRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(
        impl: VocabularyRepositoryImpl
    ): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        impl: FirebaseQuizRepository
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        impl: FirebaseQuestionRepository
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindPlacementRepository(
        impl: PlacementRepositoryImpl
    ): PlacementRepository

    @Binds
    @Singleton
    abstract fun bindAIChatRepository(
        impl: AIChatRepositoryImpl
    ): AIChatRepository
}
