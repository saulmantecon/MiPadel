package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.CommunityRepository
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Amistad
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    private val _amigos = MutableStateFlow<List<Usuario>>(emptyList())
    val amigos: StateFlow<List<Usuario>> get() = _amigos

    private val _solicitudes: MutableStateFlow<List<Pair<Amistad, Usuario>>> = MutableStateFlow(emptyList())
    val solicitudes: StateFlow<List<Pair<Amistad, Usuario>>> get() = _solicitudes

    private val _busqueda = MutableStateFlow<List<Usuario>>(emptyList())
    val busqueda: StateFlow<List<Usuario>> get() = _busqueda

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val currentUid = CurrentUserManager.getUsuario()?.uid ?: ""


    // Cargar lista de amigos
    fun loadAmigos() {
        viewModelScope.launch {
            _loading.value = true
            val result = CommunityRepository.obtenerAmigos(currentUid)
            _amigos.value = result.getOrDefault(emptyList())
            _loading.value = false
        }
    }

    private suspend fun obtenerUsuario(uid: String): Usuario? {
        val result = UsuarioRepository.obtenerUsuario(uid)
        return result.getOrNull()
    }


    // Cargar solicitudes recibidas
    fun loadSolicitudes() {
        viewModelScope.launch {
            val result = CommunityRepository.obtenerSolicitudesRecibidas(currentUid)
            val lista = result.getOrDefault(emptyList())

            // cargar usernames
            val enriched = lista.mapNotNull { amistad ->
                val usuario = obtenerUsuario(amistad.enviadoPor)
                if (usuario != null) Pair(amistad, usuario) else null
            }

            _solicitudes.value = enriched
        }
    }


    // Buscar usuarios por nombre
    fun buscarUsuarios(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _busqueda.value = emptyList()
                return@launch
            }

            val result = UsuarioRepository.buscarUsuarios(query)
            val lista = result.getOrDefault(emptyList())

            val yo = currentUid

            _busqueda.value = lista.filter { it.uid != yo }
        }
    }

    // Enviar solicitud
    fun enviarSolicitud(toUid: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = CommunityRepository.enviarSolicitud(currentUid, toUid)

            onResult(
                result.fold(
                    onSuccess = { "Solicitud enviada" },
                    onFailure = { it.message ?: "Error" }
                )
            )
        }
    }

    // Aceptar solicitud
    fun aceptarSolicitud(amistad: Amistad) {
        viewModelScope.launch {
            CommunityRepository.aceptarSolicitud(amistad.docId, amistad.user1, amistad.user2)
            loadSolicitudes()
            loadAmigos()
        }
    }

    // Rechazar solicitud
    fun rechazarSolicitud(amistad: Amistad) {
        viewModelScope.launch {
            CommunityRepository.rechazarSolicitud(amistad.docId)
            loadSolicitudes()
        }
    }

    fun eliminarAmigo(uidAmigo: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val myUid = currentUid

            val result = CommunityRepository.eliminarAmigo(myUid, uidAmigo)

            if (result.isSuccess) {
                onResult("Amigo eliminado")
                loadAmigos() // actualizar lista
            } else {
                onResult(result.exceptionOrNull()?.message ?: "Error al eliminar amigo")
            }
        }
    }

}

