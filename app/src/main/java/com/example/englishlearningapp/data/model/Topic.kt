package com.example.englishlearningapp.data.model

import com.google.firebase.firestore.PropertyName

data class Topic(
    @get:PropertyName("topicId")
    @set:PropertyName("topicId")
    var id: String = "",

    @get:PropertyName("topicName")
    @set:PropertyName("topicName")
    var name: String = "",

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String = "",

    @get:PropertyName("status")
    @set:PropertyName("status")
    var status: String = "active"
) {
    constructor() : this("", "", "", "active")
}
