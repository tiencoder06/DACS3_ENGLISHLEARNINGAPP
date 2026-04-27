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
    abstract fun bindQuestionRepository(
        firebaseQuestionRepository: FirebaseQuestionRepository
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        firebaseQuizRepository: FirebaseQuizRepository
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindTopicRepository(
        firebaseTopicRepository: FirebaseTopicRepository
    ): TopicRepository
}
