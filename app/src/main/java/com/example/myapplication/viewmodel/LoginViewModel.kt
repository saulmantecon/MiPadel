package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun loginUser(email: String, password: String, keepLogged: Boolean, settings: SettingsViewModel) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = UsuarioRepository.loginUsuario(email, password)

            result.fold(
                onSuccess = { usuario ->
                    CurrentUserManager.setUsuario(usuario)

                    if (keepLogged) {
                        settings.saveKeepLoggedIn(true)
                        settings.saveUserUid(usuario.uid)
                    } else {
                        settings.saveKeepLoggedIn(false)
                        settings.saveUserUid(null)
                    }

                    _authState.value = AuthState.Success("Bienvenido, ${usuario.nombre}")
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Error al iniciar sesión"
                    )
                }
            )
        }
    }
}
