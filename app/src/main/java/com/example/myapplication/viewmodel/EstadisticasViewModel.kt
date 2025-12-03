package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.model.Partido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapplication.data.repository.PartidoFinalizadoRepository
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.PartidoFinalizado
import com.example.myapplication.model.Usuario


class EstadisticasViewModel : ViewModel() {

    // --- Usuario actual
    val usuario = CurrentUserManager.usuario

    // --- Lista de partidos finalizados del usuario ---
    private val _partidos = MutableStateFlow<List<PartidoFinalizado>>(emptyList())
    val partidos: StateFlow<List<PartidoFinalizado>> = _partidos

    // --- Mapa de usuarios ---
    private val _usuariosMapa = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val usuariosMapa: StateFlow<Map<String, Usuario>> = _usuariosMapa


    //    CARGAR PARTIDOS
    fun cargarPartidosFinalizados(uid: String) {
        viewModelScope.launch {
            Log.d("ESTAD", "Cargando partidos finalizados para uid=$uid")

            val res = PartidoFinalizadoRepository.obtenerPartidosDeUsuario(uid)
            val lista = res.getOrDefault(emptyList())

            Log.d("ESTAD", "Encontrados ${lista.size} partidos finalizados")
            _partidos.value = lista

            // cargar usuarios relacionados
            lista.forEach { partido ->
                partido.posiciones
                    .filter { it.isNotBlank() }
                    .forEach { posUid ->
                        solicitarUsuario(posUid)
                    }
            }
        }
    }

    //    CARGAR USUARIO
    fun solicitarUsuario(uid: String) {
        if (uid.isBlank()) return

        // si ya lo tenemos, no pedimos nada
        if (_usuariosMapa.value.containsKey(uid)) return

        viewModelScope.launch {
            val res = UsuarioRepository.obtenerUsuario(uid)

            res.getOrNull()?.let { usuario ->
                // actualizar cache igual que HomeViewModel
                _usuariosMapa.value = _usuariosMapa.value.toMutableMap().apply {
                    put(uid, usuario)
                }
            }
        }
    }
}
