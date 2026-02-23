package com.ap.app.lock.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("app_lock_prefs")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val LAST_UNLOCKED_APP = stringPreferencesKey("last_unlocked_app")
        val UNLOCK_ATTEMPTS = stringPreferencesKey("unlock_attempts")
        val FAILED_ATTEMPTS = stringPreferencesKey("failed_attempts")
    }

    suspend fun saveLastUnlockedApp(packageName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_UNLOCKED_APP] = packageName
        }
    }

    fun getLastUnlockedApp(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_UNLOCKED_APP]
    }

    suspend fun recordFailedAttempt(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAILED_ATTEMPTS] ?: "0"
            preferences[PreferencesKeys.FAILED_ATTEMPTS] = (current.toInt() + 1).toString()
        }
    }

    suspend fun clearFailedAttempts() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAILED_ATTEMPTS] = "0"
        }
    }
}
