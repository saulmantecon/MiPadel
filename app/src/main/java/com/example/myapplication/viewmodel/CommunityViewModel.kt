package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.CommunityRepository
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Amistad
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    private val _amigos = MutableStateFlow<List<Usuario>>(emptyList())
    val amigos: StateFlow<List<Usuario>> get() = _amigos

    private val _solicitudes =
        MutableStateFlow<List<Pair<Amistad, Usuario>>>(emptyList())
    val solicitudes: StateFlow<List<Pair<Amistad, Usuario>>> get() = _solicitudes

    private val _busqueda = MutableStateFlow<List<Usuario>>(emptyList())
    val busqueda: StateFlow<List<Usuario>> get() = _busqueda

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val currentUid = CurrentUserManager.getUsuario()?.uid ?: ""

    // Query que estaba activa cuando se pidió la última búsqueda
    private var ultimaQuery: String = ""

    // Job para hacer debounce de la búsqueda (retrasar ejecución)
    private var searchJob: Job? = null

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

    fun loadSolicitudes() {
        viewModelScope.launch {
            val result = CommunityRepository.obtenerSolicitudesRecibidas(currentUid)
            val lista = result.getOrDefault(emptyList())

            val enriched = lista.mapNotNull { amistad ->
                val usuario = obtenerUsuario(amistad.enviadoPor)
                if (usuario != null) amistad to usuario else null
            }

            _solicitudes.value = enriched
        }
    }

    // BÚSQUEDA DE USUARIOS
    fun buscarUsuarios(query: String) {
        ultimaQuery = query

        // Cancelar búsqueda anterior, si la hubiera
        searchJob?.cancel()

        // Nueva búsqueda con pequeño retraso (debounce)
        searchJob = viewModelScope.launch {
            // Esperar un poco para evitar disparar una request por cada letra
            delay(300L)

            if (query.isBlank()) {
                _busqueda.value = emptyList()
                return@launch
            }

            val result = UsuarioRepository.buscarUsuarios(query)
            val lista = result.getOrDefault(emptyList())

            // Si el usuario cambió el texto mientras tanto, ignoramos esta respuesta
            if (query != ultimaQuery) return@launch

            val yo = currentUid
            val filtrada = mutableListOf<Usuario>()

            for (usuario in lista) {
                if (usuario.uid != yo) {
                    val estadoRes =
                        CommunityRepository.obtenerEstadoRelacion(yo, usuario.uid)

                    val estado = estadoRes.getOrNull()

                    // mostramos solo usuarios con relación "creable"
                    if (estado == null || estado == "rechazado" || estado == "eliminado") {
                        filtrada.add(usuario)
                    }
                }
            }

            // Confirmar que la query sigue siendo la actual
            if (query == ultimaQuery) {
                _busqueda.value = filtrada
            }
        }
    }

    // SOLICITUDES
    fun enviarSolicitud(toUid: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = CommunityRepository.enviarSolicitud(currentUid, toUid)

            onResult(
                result.fold(
                    onSuccess = { "Solicitud enviada" },
                    onFailure = { it.message ?: "Error" }
                )
            )

            // volver a filtrar la búsqueda actual
            if (ultimaQuery.isNotBlank()) {
                buscarUsuarios(ultimaQuery)
            }
        }
    }

    fun aceptarSolicitud(amistad: Amistad) {
        viewModelScope.launch {
            CommunityRepository.aceptarSolicitud(amistad.docId, amistad.user1, amistad.user2)
            loadSolicitudes()
            loadAmigos()
        }
    }

    fun rechazarSolicitud(amistad: Amistad) {
        viewModelScope.launch {
            CommunityRepository.rechazarSolicitud(amistad.docId)
            loadSolicitudes()
        }
    }

    fun eliminarAmigo(uidAmigo: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = CommunityRepository.eliminarAmigo(currentUid, uidAmigo)

            if (result.isSuccess) {
                onResult("Amigo eliminado")
                loadAmigos()
            } else {
                onResult(result.exceptionOrNull()?.message ?: "Error al eliminar amigo")
            }
        }
    }
}
