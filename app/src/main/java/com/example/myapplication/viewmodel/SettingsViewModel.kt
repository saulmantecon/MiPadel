package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.FirebaseAuthManager
import com.example.myapplication.data.UserPreferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel global para ajustes del usuario.
 *
 * Se encarga de:
 * - Exponer el tema (claro/oscuro/sistema)
 * - Gestionar sesión guardada
 * - Guardar UID del usuario
 * - Hacer logout limpiando DataStore y FirebaseAuth
 *
 * Este ViewModel se crea una vez en MainActivity y se mantiene
 * durante toda la vida de la aplicación.
 */
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferencesDataStore(app)

    // Indica si las preferencias ya se han leído al menos una vez
    val prefsLoaded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            // Leer valores iniciales (solo para asegurarnos de que DataStore está listo)
            prefs.keepLoggedIn.first()
            prefs.savedUserUid.first()
            prefs.themeMode.first()

            prefsLoaded.value = true
        }
    }

    // Exponer valores como StateFlow para la UI

    val themeMode = prefs.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        "system"
    )

    val keepLoggedIn = prefs.keepLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false
    )

    // Setters de preferencias
    fun setTheme(mode: String) {
        viewModelScope.launch { prefs.saveThemeMode(mode) }
    }

    fun saveKeepLoggedIn(enabled: Boolean) {
        viewModelScope.launch { prefs.saveKeepLoggedIn(enabled) }
    }

    fun saveUserUid(uid: String?) {
        viewModelScope.launch { prefs.saveUserUid(uid) }
    }

    /**
     * Cierra sesión del usuario:
     * - Limpia preferencias de sesión
     * - Hace signOut en FirebaseAuth
     * - Vacía usuario actual en memoria
     */
    fun logout() {
        viewModelScope.launch {
            prefs.clearSession()
            FirebaseAuthManager.auth.signOut()
            CurrentUserManager.clearUsuario()
        }
    }
}
