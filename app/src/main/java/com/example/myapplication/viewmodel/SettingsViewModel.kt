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
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = UserPreferencesDataStore(app)

    //  Esperar a que DataStore cargue
    val prefsLoaded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            // Esperar primeros valores reales (por si los quieres para tema, etc.)
            prefs.keepLoggedIn.first()
            prefs.savedUserUid.first()
            prefs.themeMode.first()

            prefsLoaded.value = true
        }
    }

    //  STATEFLOWS PARA LA UI
    val themeMode = prefs.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "system"
    )

    val keepLoggedIn = prefs.keepLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val savedUserUid = prefs.savedUserUid.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    //  LECTURAS DIRECTAS (para autologin)
    suspend fun readKeepLoggedOnce(): Boolean {
        return prefs.keepLoggedIn.first()
    }

    suspend fun readSavedUidOnce(): String? {
        return prefs.savedUserUid.first()
    }

    //  SETTERS DE DATASTORE
    fun setTheme(mode: String) {
        viewModelScope.launch {
            prefs.saveThemeMode(mode)
        }
    }

    fun saveKeepLoggedIn(enabled: Boolean) {
        viewModelScope.launch { prefs.saveKeepLoggedIn(enabled) }
    }

    fun saveUserUid(uid: String?) {
        viewModelScope.launch { prefs.saveUserUid(uid) }
    }

    // LOGOUT
    fun logout() {
        viewModelScope.launch {
            prefs.clearSession()
            FirebaseAuthManager.auth.signOut()
            CurrentUserManager.clearUsuario()
        }
    }
}
