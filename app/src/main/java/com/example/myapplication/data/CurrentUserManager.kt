package com.example.myapplication.data

import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrentUserManager {

    // flujo observable con el usuario actual
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> get() = _usuario

    // asignar usuario actual (desde login o registro)
    fun setUsuario(user: Usuario) {
        _usuario.value = user
    }

    // obtener usuario actual (una sola vez)
    fun getUsuario(): Usuario? = _usuario.value

    // limpiar al cerrar sesión
    fun clearUsuario() {
        _usuario.value = null
    }
}
