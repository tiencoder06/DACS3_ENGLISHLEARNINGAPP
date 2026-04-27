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
    const val QUIZ_PART_SELECTION = "quiz_part_selection/{lessonId}"
    const val QUIZ = "quiz/{lessonId}/{part}"
    const val RESULT = "result/{score}/{correct}/{total}"
    const val REVIEW = "review"
    const val REVIEW_PRACTICE = "review_practice"
    const val PROGRESS = "progress"

    // Profile
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    // Helper functions
    fun lessonList(topicId: String) = "lesson/$topicId"
    fun quizPartSelection(lessonId: String) = "quiz_part_selection/$lessonId"
    fun quiz(lessonId: String, part: Int) = "quiz/$lessonId/$part"
    fun result(score: Int, correct: Int, total: Int) = "result/$score/$correct/$total"
}
