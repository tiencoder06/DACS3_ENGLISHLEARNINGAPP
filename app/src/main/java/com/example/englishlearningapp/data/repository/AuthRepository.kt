package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    init {
        // Tối ưu hóa Firestore để phản hồi nhanh hơn khi offline hoặc mạng yếu
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestore.firestoreSettings = settings
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    suspend fun login(email: String, pass: String): Resource<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login Error: ${e.message}")
            Resource.Error(e.localizedMessage ?: "Đăng nhập thất bại")
        }
    }

    suspend fun register(email: String, pass: String, fullName: String): Resource<Boolean> {
        return try {
            // 1. Tạo User trên Auth
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val userProfile = User(
                    uid = firebaseUser.uid,
                    email = email,
                    fullName = fullName,
                    role = "user",
                    streakDays = 0,
                    wordsLearned = 0,
                    level = "Beginner",
                    createdAt = System.currentTimeMillis()
                )

                // 2. Lưu vào Firestore với Timeout 10 giây để tránh treo máy
                try {
                    withTimeout(10000) {
                        firestore.collection("users")
                            .document(firebaseUser.uid)
                            .set(userProfile)
                            .await()
                    }
                    Resource.Success(true)
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Firestore Error: ${e.message}")
                    // Nếu lỗi Firestore (do Rules chẳng hạn), vẫn trả về thành công vì Auth đã tạo xong,
                    // nhưng báo lỗi để dev biết. Hoặc bạn có thể xóa user auth ở đây nếu muốn đồng bộ tuyệt đối.
                    Resource.Error("Tài khoản đã tạo nhưng không thể lưu profile: ${e.localizedMessage}")
                }
            } else {
                Resource.Error("Không thể tạo người dùng")
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Resource.Error("Email này đã được sử dụng")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Register Error: ${e.message}")
            Resource.Error(e.localizedMessage ?: "Đăng ký thất bại")
        }
    }

    suspend fun forgotPassword(email: String): Resource<Boolean> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Không thể gửi email khôi phục")
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
