package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    // Estado observable por la UI (Compose)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registerUser(username:String, nombre: String, email: String, password: String) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }

        _authState.value = AuthState.Loading

        // Llamamos al repositorio dentro de una corrutina
        viewModelScope.launch {
            val resultado = UsuarioRepository.registrarUsuario(nombre, email, password)

            resultado.fold(
                onSuccess = { usuario ->
                    CurrentUserManager.setUsuario(usuario)
                    _authState.value = AuthState.Success("Registro exitoso")
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Error al registrar usuario"
                    )
                }
            )
        }
    }
}