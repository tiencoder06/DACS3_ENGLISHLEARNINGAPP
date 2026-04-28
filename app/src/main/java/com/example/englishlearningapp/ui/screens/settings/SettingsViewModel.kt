package com.example.englishlearningapp.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.englishlearningapp.data.local.PreferenceManager
import com.example.englishlearningapp.data.repository.UserRepository
import com.example.englishlearningapp.utils.NotificationHelper
import com.example.englishlearningapp.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val userRepository: UserRepository,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "SettingsViewModel"

    val dailyReminderEnabled: StateFlow<Boolean> = preferenceManager.dailyReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderHour: StateFlow<Int> = preferenceManager.reminderHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 19)

    val reminderMinute: StateFlow<Int> = preferenceManager.reminderMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleDailyReminder(enabled: Boolean) {
        Log.d(TAG, "toggleDailyReminder: enabled = $enabled")
        viewModelScope.launch {
            preferenceManager.setDailyReminderEnabled(enabled)
            if (enabled) {
                if (checkNotificationPermission()) {
                    scheduleReminder(reminderHour.value, reminderMinute.value)
                } else {
                    Log.w(TAG, "toggleDailyReminder: Permission missing.")
                    Toast.makeText(context, "Vui lòng cấp quyền thông báo trong cài đặt!", Toast.LENGTH_LONG).show()
                }
            } else {
                cancelReminder()
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        Log.d(TAG, "updateReminderTime: $hour:$minute")
        viewModelScope.launch {
            preferenceManager.setReminderTime(hour, minute)
            if (dailyReminderEnabled.value) {
                scheduleReminder(hour, minute)
            }
        }
    }

    fun testNotificationImmediate() {
        Log.d(TAG, "testNotificationImmediate: Triggering immediate direct notification")
        if (!checkNotificationPermission()) {
            Toast.makeText(context, "Chưa có quyền thông báo!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            // Lấy thông tin user để nội dung test giống thật nhất
            val userResource = userRepository.getUserProfile()
            val body = if (userResource is com.example.englishlearningapp.utils.Resource.Success) {
                val user = userResource.data
                when {
                    user.difficultWords.isNotEmpty() -> "Bạn còn một số từ khó cần ôn tập lại hôm nay."
                    else -> "Hôm nay bạn chưa học bài nào. Hãy tiếp tục chuỗi học nhé!"
                }
            } else {
                "Hôm nay bạn chưa học bài nào. Hãy tiếp tục chuỗi học nhé!"
            }

            notificationHelper.showNotification(
                "Nhắc học tiếng Anh",
                body
            )
        }
    }

    fun testNotificationOneMinute() {
        Log.d(TAG, "testNotificationOneMinute: Scheduling work with 1 minute delay")
        if (!checkNotificationPermission()) {
            Toast.makeText(context, "Chưa có quyền thông báo!", Toast.LENGTH_SHORT).show()
            return
        }

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .addTag(ReminderWorker.TAG_TEST)
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            "one_minute_test_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Toast.makeText(context, "Thông báo thực tế sẽ hiện sau 1 phút nữa.", Toast.LENGTH_SHORT).show()
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
        Log.d(TAG, "scheduleReminder: Periodic work scheduled. Delay = ${initialDelay/1000}s")

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_reminder_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelReminder() {
        Log.d(TAG, "cancelReminder: Cancelling production work")
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminder_work")
    }
}
