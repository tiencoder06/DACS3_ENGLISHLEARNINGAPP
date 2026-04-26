package com.example.englishlearningapp.data.model

import com.google.firebase.firestore.PropertyName

data class Vocabulary(
    @get:PropertyName("vocabId")
    @set:PropertyName("vocabId")
    var id: String = "",

    @get:PropertyName("lessonId")
    @set:PropertyName("lessonId")
    var lessonId: String = "",

    @get:PropertyName("word")
    @set:PropertyName("word")
    var word: String = "",

    @get:PropertyName("meaning")
    @set:PropertyName("meaning")
    var meaning: String = "",

    @get:PropertyName("pronunciation")
    @set:PropertyName("pronunciation")
    var pronunciation: String = "",

    @get:PropertyName("partOfSpeech")
    @set:PropertyName("partOfSpeech")
    var partOfSpeech: String = "",

    @get:PropertyName("exampleSentence")
    @set:PropertyName("exampleSentence")
    var exampleSentence: String = "",

    @get:PropertyName("audioText")
    @set:PropertyName("audioText")
    var audioText: String = "",

    @get:PropertyName("audioUrl")
    @set:PropertyName("audioUrl")
    var audioUrl: String = "",

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "active"
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "active")
}
