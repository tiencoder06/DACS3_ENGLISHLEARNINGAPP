package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getCurrentUser() = firebaseAuth.currentUser

    suspend fun login(email: String, pass: String): Resource<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed")
        }
    }

    suspend fun register(email: String, pass: String, fullName: String): Resource<Boolean> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Tạo profile trên Firestore ngay sau khi Auth thành công
                val userProfile = User(
                    uid = firebaseUser.uid,
                    email = email,
                    fullName = fullName,
                    createdAt = System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(userProfile)
                    .await()
                Resource.Success(true)
            } else {
                Resource.Error("User registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    suspend fun forgotPassword(email: String): Resource<Boolean> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send reset email")
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
