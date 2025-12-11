package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.PartidoFinalizadoRepository
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.PartidoFinalizado
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EstadisticasViewModel : ViewModel() {

    // Usuario logeado
    val usuario = CurrentUserManager.usuario

    // Lista de partidos finalizados
    private val _partidos = MutableStateFlow<List<PartidoFinalizado>>(emptyList())
    val partidos: StateFlow<List<PartidoFinalizado>> = _partidos

    //usuarios que aparecen en los partidos.
    private val _usuariosMapa = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val usuariosMapa: StateFlow<Map<String, Usuario>> = _usuariosMapa

    // CARGAR PARTIDOS FINALIZADOS DEL USUARIO
    fun cargarPartidosFinalizados(uid: String) {
        viewModelScope.launch {

            val res = PartidoFinalizadoRepository.obtenerPartidosDeUsuario(uid)
            val lista = res.getOrDefault(emptyList())

            _partidos.value = lista

            // Cargar usuarios implicados
            lista.forEach { partido ->
                partido.posiciones
                    .filter { it.isNotBlank() }
                    .forEach { solicitarUsuario(it) }
            }
        }
    }


    // OBTENER DATOS DE UN USUARIO
    fun solicitarUsuario(uid: String) {
        if (uid.isBlank()) return
        if (_usuariosMapa.value.containsKey(uid)) return

        viewModelScope.launch {
            UsuarioRepository.obtenerUsuario(uid)
                .getOrNull()
                ?.let { usuario ->
                    _usuariosMapa.value = _usuariosMapa.value.toMutableMap().apply {
                        put(uid, usuario)
                    }
                }
        }
    }

    // RECARGAR ESTADÍSTICAS DEL USUARIO
    fun recargarUsuario(uid: String) {
        viewModelScope.launch {
            UsuarioRepository.obtenerUsuario(uid)
                .getOrNull()
                ?.let { actualizado ->
                    CurrentUserManager.setUsuario(actualizado)
                }
        }
    }
}
