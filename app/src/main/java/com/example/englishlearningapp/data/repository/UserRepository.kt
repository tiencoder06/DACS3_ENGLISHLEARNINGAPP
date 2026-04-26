package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getUserProfile(): Resource<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User profile not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch profile")
        }
    }
}
