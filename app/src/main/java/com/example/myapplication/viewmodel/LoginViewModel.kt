package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseAuthManager
import com.example.myapplication.model.AuthState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuthManager.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Por favor, completa todos los campos")
            return
        }
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success("Inicio de sesión exitoso")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}