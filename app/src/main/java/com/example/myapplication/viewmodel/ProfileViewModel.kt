package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    val usuario = CurrentUserManager.usuario

    suspend fun updateUsuario(usuario: Usuario): String {
        return try {
            val result = UsuarioRepository.updateUsuario(usuario)
            result.fold(
                onSuccess = {
                    // Esto ya actualiza el flujo global automáticamente
                    CurrentUserManager.setUsuario(usuario)
                    "Perfil actualizado correctamente."
                },
                onFailure = { e ->
                    "Error al actualizar usuario: ${e.message ?: "desconocido"}"
                }
            )
        } catch (e: Exception) {
            "Error al actualizar usuario: ${e.message ?: "desconocido"}"
        }
    }
}
