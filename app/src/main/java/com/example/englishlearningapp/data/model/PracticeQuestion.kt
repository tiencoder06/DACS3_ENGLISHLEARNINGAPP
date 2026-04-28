package com.example.englishlearningapp.data.model

enum class PracticeType {
    EN_TO_VI,    // English -> Vietnamese
    VI_TO_EN,    // Vietnamese -> English
    AUDIO_TO_VI, // Audio -> Vietnamese
    FILL_BLANK,  // Typing English word
    MATCHING,    // Pair matching
    SENTENCE_REORDER // New: Reorder words to form a sentence
}

data class PracticeQuestion(
    val vocabulary: Vocabulary,
    val type: PracticeType,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    // For Matching
    val matchingPairs: List<Pair<String, String>> = emptyList(),
    // For Sentence Reorder
    val shuffledWords: List<String> = emptyList()
)
