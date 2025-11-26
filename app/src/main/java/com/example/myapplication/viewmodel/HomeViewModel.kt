package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Partido
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.HomeRepository
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class HomeViewModel : ViewModel() {

    private val _partidos = MutableStateFlow<List<Partido>>(emptyList())
    val partidos: StateFlow<List<Partido>> = _partidos.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // cache de usuarios (para fotos + username)
    private val _usuarios = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val usuarios: StateFlow<Map<String, Usuario>> = _usuarios.asStateFlow()

    val currentUid: String = CurrentUserManager.getUsuario()?.uid ?: ""

    init {
        escucharPartidos()
    }

    private fun escucharPartidos() {
        viewModelScope.launch {
            HomeRepository.escucharPartidos().collectLatest { lista ->
                _partidos.value = lista
            }
        }
    }

    fun solicitarUsuario(uid: String) {
        if (uid.isBlank()) return

        if (_usuarios.value.containsKey(uid)) return

        viewModelScope.launch {
            val res = UsuarioRepository.obtenerUsuario(uid)
            res.getOrNull()?.let { usuario ->
                _usuarios.value = _usuarios.value.toMutableMap().apply {
                    put(uid, usuario)
                }
            }
        }
    }

    fun ocuparPosicion(partido: Partido, slot: Int) {
        val uid = currentUid

        // 1. Si ya está dentro del partido, no hacemos nada
        if (partido.posiciones.contains(uid)) {
            return
        }

        // 2. Si el hueco ya está ocupado → no hacemos nada
        if (partido.posiciones[slot].isNotEmpty()) {
            return
        }

        // 3. Ahora sí -> unirse
        viewModelScope.launch {
            val result = HomeRepository.ocuparPosicion(partido.id, slot, uid)
            _mensaje.value = result.fold(
                onSuccess = { "Te uniste al partido" },
                onFailure = { it.message ?: "Error al unirse" }
            )
        }
    }


    fun salirDePartido(partidoId: String) {
        viewModelScope.launch {
            val res = HomeRepository.salirDePartido(partidoId, currentUid)
            _mensaje.value = res.fold(
                onSuccess = { "Has salido del partido" },
                onFailure = { it.message ?: "No puedes salir" }
            )
        }
    }

    fun borrarPartido(partidoId: String) {
        viewModelScope.launch {
            val res = HomeRepository.borrarPartido(partidoId, currentUid)
            _mensaje.value = res.fold(
                onSuccess = { "Partido eliminado" },
                onFailure = { it.message ?: "Error al borrar partido" }
            )
        }
    }

    fun mostrarMensaje(msg: String) {
        _mensaje.value = msg
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}