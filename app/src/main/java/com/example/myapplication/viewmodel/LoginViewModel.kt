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

    // Expone el estado de la autenticación a la UI
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Login del usuario:
     * 1) Validación local
     * 2) Llamada al repositorio
     * 3) Actualización de CurrentUserManager
     * 4) Guardado en DataStore si procede
     */
    fun loginUser(
        email: String,
        password: String,
        keepLogged: Boolean,
        settings: SettingsViewModel
    ) {
        // Validación
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = UsuarioRepository.loginUsuario(email, password)

            result.fold(
                onSuccess = { usuario ->

                    // Guardar usuario en memoria global
                    CurrentUserManager.setUsuario(usuario)

                    // Persistencia de sesión
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
                    val mensaje = when {
                        e.message?.contains("password is invalid", ignoreCase = true) == true ->
                            "La contraseña es incorrecta."

                        e.message?.contains("no user record", ignoreCase = true) == true ->
                            "No existe ninguna cuenta con este correo."

                        e.message?.contains("badly formatted", ignoreCase = true) == true ->
                            "El correo introducido no es válido."

                        else ->
                            "No se ha podido iniciar sesión. Inténtalo de nuevo."
                    }
                    _authState.value = AuthState.Error(mensaje)
                }
            )
        }
    }
}
