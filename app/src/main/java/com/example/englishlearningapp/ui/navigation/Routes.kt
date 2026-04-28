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
    const val VOCABULARY_DETAIL = "vocabulary_detail/{lessonId}"

    // Learning & Quiz
    const val PRACTICE = "practice/{lessonId}"
    const val QUIZ = "quiz/{lessonId}"
    const val RESULT = "result/{score}/{correct}/{total}"
    const val REVIEW = "review"
    const val PROGRESS = "progress"

    // Profile
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"

    // Placement Test
    const val PLACEMENT_INTRO = "placement_intro"
    const val PLACEMENT_QUESTION = "placement_question"
    const val PLACEMENT_RESULT = "placement_result"

    // Helper functions
    fun lessonList(topicId: String) = "lesson/$topicId"
    fun vocabularyList(lessonId: String) = "vocabulary_list/$lessonId"
    fun vocabularyDetail(lessonId: String) = "vocabulary_detail/$lessonId"
    fun practice(lessonId: String) = "practice/$lessonId"
    fun quiz(lessonId: String) = "quiz/$lessonId"
    fun result(score: Int, correct: Int, total: Int) = "result/$score/$correct/$total"
}
