package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    //MutableStateFLow: sirve para mantener un valor observable que puede cambiar con el tiempo (en este caso el estado de autenticación).
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    //StateFLow: sirve para exponer el estado de autenticación a la vista.
    val authState: StateFlow<AuthState> = _authState

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        //Fuera de la corrutina para mostrar el loading
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = UsuarioRepository.loginUsuario(email, password)

            result.fold(onSuccess = { usuario ->
                _authState.value = AuthState.Success("Bienvenido, ${usuario.nombre}")
                // Aquí podrías guardar el usuario actual si quieres (por ejemplo, en DataStore)
            }, onFailure = { e ->
                _authState.value = AuthState.Error(
                    e.message ?: "Error al iniciar sesión"
                )
            })
        }
    }
}