package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore una sola vez por contexto
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * UserPreferencesDataStore
 *
 * Clase encargada de gestionar las preferencias persistentes del usuario:
 * - Mantener la sesión iniciada
 * - Guardar el UID del usuario
 * - Guardar el tema seleccionado (claro/oscuro/sistema)
 *
 * El uso de DataStore  es moderno, seguro ante corrupciones
 */
class UserPreferencesDataStore(private val context: Context) {

    // Keys usadas para guardar valores
    companion object {
        val KEY_KEEP_LOGGED_IN = booleanPreferencesKey("keep_logged_in")
        val KEY_SAVED_UID = stringPreferencesKey("saved_user_uid")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    //LECTURA DE VALORES COMO FLOW

    // Si no existe, devolvemos false
    val keepLoggedIn: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_KEEP_LOGGED_IN] ?: false
    }

    // UID puede ser null si no hay usuario guardado
    val savedUserUid: Flow<String?> = context.dataStore.data.map {
        it[KEY_SAVED_UID]
    }

    // Tema: "light", "dark" o "system"
    val themeMode: Flow<String> = context.dataStore.data.map {
        it[KEY_THEME_MODE] ?: "system"
    }

    //MÉTODOS DE ESCRITURA

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

    /**
     * Limpia la sesión:
     * - No mantiene sesión
     * - No guarda UID
     * (El tema se mantiene porque es una preferencia del dispositivo)
     */
    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(KEY_KEEP_LOGGED_IN)
            it.remove(KEY_SAVED_UID)
        }
    }
}
