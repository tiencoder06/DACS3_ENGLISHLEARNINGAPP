package com.example.englishlearningapp.data.model

import com.google.firebase.firestore.PropertyName

data class Lesson(
    @get:PropertyName("lessonId")
    @set:PropertyName("lessonId")
    var id: String = "",

    @get:PropertyName("topicId")
    @set:PropertyName("topicId")
    var topicId: String = "",

    @get:PropertyName("lessonName")
    @set:PropertyName("lessonName")
    var name: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("order")
    @set:PropertyName("order")
    var order: Int = 0,

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "active"
) {
    constructor() : this("", "", "", "", 0, "active")
}
