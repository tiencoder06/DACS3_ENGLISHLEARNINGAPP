package com.example.englishlearningapp.ui.navigation

object Routes {
    // Auth & System
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Main Content
    const val HOME = "home"
    const val TOPIC = "topic"
    const val LESSON = "lesson/{topicId}"
    const val VOCABULARY_LIST = "vocabulary_list/{lessonId}"

    // Learning & Quiz
    const val PRACTICE = "practice/{lessonId}"
    const val QUIZ = "quiz/{lessonId}"
    const val RESULT = "result/{score}/{correct}/{total}"
    const val REVIEW = "review"
    const val PROGRESS = "progress"

    // Profile
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    // Helper functions
    fun lessonList(topicId: String) = "lesson/$topicId"
    fun quiz(lessonId: String) = "quiz/$lessonId"
    fun result(score: Int, correct: Int, total: Int) = "result/$score/$correct/$total"
}
