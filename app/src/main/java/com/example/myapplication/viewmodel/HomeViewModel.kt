package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Partido
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.HomeRepository
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.SetResult
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.max
import kotlin.math.min
class HomeViewModel : ViewModel() {

    private val _partidos = MutableStateFlow<List<Partido>>(emptyList())
    val partidos: StateFlow<List<Partido>> = _partidos.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // cache de usuarios
    private val _usuarios = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val usuarios: StateFlow<Map<String, Usuario>> = _usuarios.asStateFlow()

    val currentUid: String = CurrentUserManager.getUsuario()?.uid ?: ""

    init {
        escucharPartidos()
        iniciarRelojEstados()
    }

    /** Escucha Firestore y solo sincroniza la lista de partidos. */
    private fun escucharPartidos() {
        viewModelScope.launch {
            HomeRepository.escucharPartidos().collectLatest { listaRaw ->
                _partidos.value = listaRaw
            }
        }
    }

    /** Reloj interno: cada 30s recalcula el estado de todos los partidos. */
    private fun iniciarRelojEstados() {
        viewModelScope.launch {
            while (true) {
                actualizarEstadosSegunHora()
                kotlinx.coroutines.delay(30_000L) // cada 30 segundos
            }
        }
    }

    /** Mira la hora actual y decide: pendiente / listo / jugando / cancelado. */
    private suspend fun actualizarEstadosSegunHora() {
        val ahora = Date().time


        val listaActual = _partidos.value
        val nuevaLista = mutableListOf<Partido>()

        for (p in listaActual) {
            val fechaMillis = p.fecha?.toDate()?.time ?: Long.MAX_VALUE
            val jugadores = p.posiciones.count { it.isNotBlank() }

            val desiredEstado = when {
                p.estado == "finalizado" -> "finalizado"
                fechaMillis <= ahora && jugadores < 4 -> "cancelado"
                fechaMillis <= ahora && jugadores == 4 -> "jugando"
                jugadores == 4 -> "listo"
                else -> "pendiente"
            }

            when (desiredEstado) {
                "cancelado" -> {
                    // se elimina automáticamente en Firestore
                    if (p.id.isNotBlank()) {
                        HomeRepository.cancelarPorTiempo(p.id)
                    }
                    // NO lo añadimos a nuevaLista porque deja de existir
                }

                else -> {
                    // si ha cambiado el estado en Firestore lo actualizamos
                    if (desiredEstado != p.estado && p.id.isNotBlank()) {
                        HomeRepository.actualizarEstado(p.id, desiredEstado)
                        nuevaLista.add(p.copy(estado = desiredEstado))
                    } else {
                        nuevaLista.add(p)
                    }
                }
            }
        }

        _partidos.value = nuevaLista
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

        if (partido.estado == "jugando" || partido.estado == "finalizado") {
            _mensaje.value = if (partido.estado == "jugando")
                "El partido ya está en juego"
            else
                "El partido ya ha finalizado"
            return
        }

        if (partido.posiciones.contains(uid)) return
        if (partido.posiciones[slot].isNotEmpty()) return

        viewModelScope.launch {
            val result = HomeRepository.ocuparPosicion(partido.id, slot, uid)
            _mensaje.value = result.fold(
                onSuccess = { "Te uniste al partido" },
                onFailure = { it.message ?: "Error al unirse" }
            )
        }
    }
    fun salirDePartido(partido: Partido) {
        // Bloquear salir si el partido ya está en juego
        if (partido.estado == "jugando") {
            _mensaje.value = "No puedes salir de un partido que está en juego"
            return
        }

        viewModelScope.launch {
            val res = HomeRepository.salirDePartido(partido.id, currentUid)
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

    //Validación de sets y finalización del partido


    fun finalizarPartido(partido: Partido, setsInput: List<Pair<String, String>>) {
        viewModelScope.launch {
            if (currentUid != partido.creadorId) {
                _mensaje.value = "Solo el creador puede finalizar el partido"
                return@launch
            }

            if (partido.estado != "jugando") {
                _mensaje.value = "Solo puedes finalizar partidos en juego"
                return@launch
            }

            val sets = mutableListOf<SetResult>()

            for ((index, par) in setsInput.withIndex()) {
                val (s1, s2) = par
                if (s1.isBlank() && s2.isBlank()) continue

                if (s1.isBlank() || s2.isBlank()) {
                    _mensaje.value = "Completa ambos marcadores en el set ${index + 1}"
                    return@launch
                }

                val j1 = s1.toIntOrNull()
                val j2 = s2.toIntOrNull()

                if (j1 == null || j2 == null) {
                    _mensaje.value = "Los juegos deben ser números en el set ${index + 1}"
                    return@launch
                }

                if (!esSetValido(j1, j2)) {
                    _mensaje.value = "Marcador inválido en el set ${index + 1}"
                    return@launch
                }

                sets.add(SetResult(juegosEquipo1 = j1, juegosEquipo2 = j2))
            }

            if (sets.isEmpty()) {
                _mensaje.value = "Introduce al menos un set"
                return@launch
            }

            if (!validarLogicaSets(sets)) {
                _mensaje.value = "Los sets no forman un resultado válido (mejor de 3)"
                return@launch
            }

            val res = HomeRepository.finalizarPartido(partido.id, sets)
            _mensaje.value = res.fold(
                onSuccess = { "Partido finalizado" },
                onFailure = { it.message ?: "Error al finalizar partido" }
            )
        }
    }

    private fun esSetValido(j1: Int, j2: Int): Boolean {
        if (j1 == j2) return false
        val maxJ = max(j1, j2)
        val minJ = min(j1, j2)

        return when {
            maxJ == 6 && minJ in 0..4 -> true      // 6-0 a 6-4
            maxJ == 7 && (minJ == 5 || minJ == 6) -> true // 7-5 o 7-6
            else -> false
        }
    }

    private fun validarLogicaSets(sets: List<SetResult>): Boolean {
        if (sets.size !in 1..3) return false

        var wins1 = 0
        var wins2 = 0

        sets.forEachIndexed { index, set ->
            if (set.juegosEquipo1 > set.juegosEquipo2) wins1++ else wins2++

            if ((wins1 == 2 || wins2 == 2) && index < sets.lastIndex) {
                return false // partido ya decidido pero hay sets extra
            }
        }

        if (wins1 == wins2) return false
        if (wins1 > 2 || wins2 > 2) return false

        return true
    }
}
