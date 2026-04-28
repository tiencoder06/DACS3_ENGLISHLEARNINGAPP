package com.example.englishlearningapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.englishlearningapp.MainActivity
import com.example.englishlearningapp.R
import com.example.englishlearningapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val uid = auth.currentUser?.uid ?: return Result.success()
        
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            
            if (user != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date())
                
                if (user.lastStudyDate != today) {
                    sendNotification(user)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendNotification(user: User) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở học tập",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val body = when {
            user.difficultWords.isNotEmpty() -> "Bạn còn một số từ khó cần ôn lại hôm nay."
            else -> "Hôm nay bạn chưa học bài nào. Hãy tiếp tục chuỗi học nhé!"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
            .setContentTitle("Nhắc học tiếng Anh")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
