package com.example.englishlearningapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.englishlearningapp.data.model.User
import com.example.englishlearningapp.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ReminderDebug"
        const val TAG_TEST = "test_reminder"
        const val TAG_IMMEDIATE = "immediate_reminder"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "ReminderWorker: Bắt đầu thực hiện tác vụ (ID: $id)")
        
        val isTest = tags.contains(TAG_TEST)
        val isImmediate = tags.contains(TAG_IMMEDIATE)

        // Nếu là lệnh Test, bỏ qua kiểm tra Firestore
        if (isImmediate || isTest) {
            Log.d(TAG, "ReminderWorker: Chế độ TEST - Gửi thông báo ngay.")
            notificationHelper.showNotification(
                "Nhắc học tiếng Anh",
                "Đây là thông báo thử nghiệm để kiểm tra hệ thống."
            )
            return Result.success()
        }

        val uid = auth.currentUser?.uid ?: return Result.success()

        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)

            if (user != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date())
                
                Log.d(TAG, "ReminderWorker: Kiểm tra ngày học. Hôm nay: $today, Lần cuối: ${user.lastStudyDate}")

                if (user.lastStudyDate != today) {
                    val body = if (user.difficultWords.isNotEmpty()) 
                        "Bạn còn một số từ khó cần ôn tập lại hôm nay."
                    else "Hôm nay bạn chưa học bài nào. Hãy tiếp tục chuỗi học nhé!"
                    
                    notificationHelper.showNotification("Nhắc học tiếng Anh", body)
                    Log.d(TAG, "ReminderWorker: Đã gửi thông báo thật.")
                } else {
                    Log.d(TAG, "ReminderWorker: Hôm nay bạn học rồi, không làm phiền nữa.")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ReminderWorker: Lỗi kết nối Firestore: ${e.message}")
            Result.failure()
        }
    }
}
