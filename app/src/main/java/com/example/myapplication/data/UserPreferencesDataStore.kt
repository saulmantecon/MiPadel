package com.example.myapplication.data


import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_KEEP_LOGGED_IN = booleanPreferencesKey("keep_logged_in")
        val KEY_SAVED_UID = stringPreferencesKey("saved_user_uid")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    // ---------------- READ ---------------- //

    val keepLoggedIn: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_KEEP_LOGGED_IN] ?: false
    }

    val savedUserUid: Flow<String?> = context.dataStore.data.map {
        it[KEY_SAVED_UID]
    }

    val themeMode: Flow<String> = context.dataStore.data.map {
        it[KEY_THEME_MODE] ?: "system"
    }

    // ---------------- WRITE ---------------- //

    suspend fun saveKeepLoggedIn(enabled: Boolean) {
        context.dataStore.edit {
            it[KEY_KEEP_LOGGED_IN] = enabled
        }
    }

    suspend fun saveUserUid(uid: String?) {
        context.dataStore.edit {
            if (uid == null) it.remove(KEY_SAVED_UID)
            else it[KEY_SAVED_UID] = uid
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit {
            it[KEY_THEME_MODE] = mode
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(KEY_KEEP_LOGGED_IN)
            it.remove(KEY_SAVED_UID)
        }
    }

    suspend fun toggleThemeMode(current: String) {
        val newMode = when (current) {
            "light" -> "dark"
            "dark" -> "system"
            else -> "light"
        }
        saveThemeMode(newMode)
    }

}