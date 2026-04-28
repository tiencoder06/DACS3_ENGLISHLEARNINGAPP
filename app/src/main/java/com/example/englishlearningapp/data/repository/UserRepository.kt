package com.example.englishlearningapp.data.repository

import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
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

    suspend fun updateFullName(newName: String): Resource<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            firestore.collection("users").document(uid)
                .update("fullName", newName)
                .await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Update failed")
        }
    }

    suspend fun recordStudyActivity(): Resource<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val userRef = firestore.collection("users").document(uid)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val user = snapshot.toObject(User::class.java) ?: return@runTransaction

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date())
                
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = dateFormat.format(calendar.time)

                var newStreakDays = user.streakDays
                var newLongestStreak = user.longestStreak
                val lastStudyDate = user.lastStudyDate

                when {
                    lastStudyDate.isEmpty() -> {
                        newStreakDays = 1
                    }
                    lastStudyDate == today -> {
                        // Already studied today, just return from transaction without changes to streak
                        return@runTransaction
                    }
                    lastStudyDate == yesterday -> {
                        newStreakDays += 1
                    }
                    else -> {
                        // Gap more than 1 day
                        newStreakDays = 1
                    }
                }

                if (newStreakDays > newLongestStreak) {
                    newLongestStreak = newStreakDays
                }

                transaction.update(userRef, mapOf(
                    "streakDays" to newStreakDays,
                    "longestStreak" to newLongestStreak,
                    "lastStudyDate" to today,
                    "lastActiveAt" to Timestamp.now()
                ))
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update streak")
        }
    }
}
