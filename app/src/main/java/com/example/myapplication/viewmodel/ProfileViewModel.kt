package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    val usuario: StateFlow<Usuario?> = CurrentUserManager.usuario

    fun updateUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                UsuarioRepository.updateUsuario(usuario)
                CurrentUserManager.setUsuario(usuario)
                println(" Usuario actualizado correctamente")
            } catch (e: Exception) {
                println(" Error al actualizar usuario: ${e.message}")
            }
        }
    }
}
