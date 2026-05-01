package com.example.englishlearningapp.data.repository

import android.util.Log
import com.example.englishlearningapp.data.model.AIMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AIChatRepository {

    override fun getChatMessages(userId: String): Flow<Result<List<AIMessage>>> = callbackFlow {
        Log.d("AIChatRepo", "Subscribing to messages for user: $userId")
        val subscription = firestore.collection("ai_chats")
            .document(userId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AIChatRepo", "Firestore error: ${error.message}")
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(AIMessage::class.java)
                    Log.d("AIChatRepo", "Received ${messages.size} messages from Firestore")
                    trySend(Result.success(messages))
                }
            }
        awaitClose { 
            Log.d("AIChatRepo", "Unsubscribing from messages")
            subscription.remove() 
        }
    }

    override suspend fun saveMessage(userId: String, message: AIMessage): Result<Unit> {
        return try {
            // Đảm bảo mỗi tin nhắn có timestamp mới nhất khi lưu
            val messageToSave = if (message.timestamp == 0L) {
                message.copy(timestamp = System.currentTimeMillis())
            } else message

            firestore.collection("ai_chats")
                .document(userId)
                .collection("messages")
                .add(messageToSave)
                .await()
            Log.d("AIChatRepo", "Message saved successfully: ${message.role}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AIChatRepo", "Error saving message", e)
            Result.failure(e)
        }
    }
}
