package com.example.englishlearningapp.ui.navigation



object Routes {
    // Auth & System (Người 1)
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Main Content (Người 2)
    const val HOME = "home"
    const val TOPIC = "topic"
    const val LESSON = "lesson/{topicId}"
    const val VOCABULARY_LIST = "vocabulary_list/{lessonId}"

    // Learning & Quiz (Người 3)
    const val PRACTICE = "practice/{lessonId}"
    const val QUIZ = "quiz/{lessonId}"
    const val RESULT = "result"
    const val PROGRESS = "progress"

    // Profile (Người 1)
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"

    // Helper functions để truyền tham số
    fun lessonList(topicId: String) = "lesson/$topicId"
    fun quiz(lessonId: String) = "quiz/$lessonId"
}
