package com.example.englishlearningapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dailyReminderEnabledKey = booleanPreferencesKey("daily_reminder_enabled")
    private val reminderHourKey = intPreferencesKey("reminder_hour")
    private val reminderMinuteKey = intPreferencesKey("reminder_minute")

    val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[dailyReminderEnabledKey] ?: false
    }

    val reminderHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[reminderHourKey] ?: 19 // Default 7 PM
    }

    val reminderMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[reminderMinuteKey] ?: 0
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[dailyReminderEnabledKey] = enabled
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[reminderHourKey] = hour
            preferences[reminderMinuteKey] = minute
        }
    }
}
