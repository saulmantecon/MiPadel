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

    // ---------------------------------------------------------
    // UID del usuario actual
    // ---------------------------------------------------------
    val currentUid = CurrentUserManager.getUsuario()?.uid ?: ""

    // ---------------------------------------------------------
    // 1. Lista de partidos (tiempo real)
    // ---------------------------------------------------------
    private val _partidos = MutableStateFlow<List<Partido>>(emptyList())
    val partidos: StateFlow<List<Partido>> get() = _partidos.asStateFlow()

    // ---------------------------------------------------------
    // 2. Caché local de usuarios (para no pedirlos 1000 veces)
    // ---------------------------------------------------------
    private val usuariosCache = mutableMapOf<String, Usuario>()

    // ---------------------------------------------------------
    // 3. Mensajes (snackbar)
    // ---------------------------------------------------------
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> get() = _mensaje.asStateFlow()


    init {
        escucharPartidos()
    }

    // ---------------------------------------------------------
    // ESCUCHAR PARTIDOS EN TIEMPO REAL
    // ---------------------------------------------------------
    private fun escucharPartidos() {
        viewModelScope.launch {
            HomeRepository.escucharPartidos().collectLatest { lista ->
                _partidos.value = lista
            }
        }
    }

    // ---------------------------------------------------------
    // OBTENER JUGADORES DE UN PARTIDO (con foto + username)
    // ---------------------------------------------------------
    fun obtenerJugadores(partido: Partido): List<Usuario> {
        val lista = mutableListOf<Usuario>()

        partido.jugadores.forEach { uid ->
            val usuario = usuariosCache[uid]

            if (usuario != null) {
                lista.add(usuario)
            } else {
                // Cargar el usuario y cachearlo
                viewModelScope.launch {
                    val res = UsuarioRepository.obtenerUsuario(uid)
                    res.getOrNull()?.let {
                        usuariosCache[uid] = it
                        // Actualizar UI re-emitiendo
                        _partidos.value = _partidos.value
                    }
                }
            }
        }

        return lista
    }

    // ---------------------------------------------------------
    // OCUPAR HUECO
    // ---------------------------------------------------------
    fun ocuparHueco(partidoId: String, slot: Int) {
        viewModelScope.launch {
            val result = HomeRepository.ocuparHueco(partidoId, slot, currentUid)

            _mensaje.value = result.fold(
                onSuccess = { "Te uniste al partido" },
                onFailure = { it.message ?: "Error al ocupar hueco" }
            )
        }
    }

    // ---------------------------------------------------------
    // SALIR DE UN HUECO
    // ---------------------------------------------------------
    fun salirDeHueco(partidoId: String, slot: Int) {
        viewModelScope.launch {
            val result = HomeRepository.salirDeHueco(partidoId, currentUid)

            _mensaje.value = result.fold(
                onSuccess = { "Has salido del partido" },
                onFailure = { it.message ?: "No puedes salir del partido" }
            )
        }
    }

    // ---------------------------------------------------------
    // BORRAR PARTIDO (solo si eres el creador)
    // ---------------------------------------------------------
    fun borrarPartido(partidoId: String) {
        viewModelScope.launch {
            val result = HomeRepository.borrarPartido(partidoId, currentUid)

            _mensaje.value = result.fold(
                onSuccess = { "Partido eliminado" },
                onFailure = { it.message ?: "Error al borrar partido" }
            )
        }
    }

    // ---------------------------------------------------------
    // LIMPIAR MENSAJE
    // ---------------------------------------------------------
    fun limpiarMensaje() {
        _mensaje.value = null
    }
}