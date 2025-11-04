package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseAuthManager
import com.example.myapplication.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuthManager.auth

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    fun register(email: String, password: String) {
        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _registerState.value = AuthState.Success("Usuario registrado correctamente")
            }catch (e: Exception){
                _registerState.value = AuthState.Error(e.message ?: "Error al registrar usuario")
            }
        }
    }
}
