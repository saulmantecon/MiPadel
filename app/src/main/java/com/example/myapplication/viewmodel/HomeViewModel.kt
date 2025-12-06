package com.example.myapplication.viewmodel

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

    // cache de usuarios
    private val _usuarios = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val usuarios: StateFlow<Map<String, Usuario>> = _usuarios.asStateFlow()

    val currentUid: String = CurrentUserManager.getUsuario()?.uid ?: ""

    init {
        escucharPartidos()
        iniciarRelojEstados()
    }

    private fun escucharPartidos() {
        viewModelScope.launch {
            HomeRepository.escucharPartidos().collectLatest { listaRaw ->
                _partidos.value = listaRaw
            }
        }
    }

    private fun iniciarRelojEstados() {
        viewModelScope.launch {
            while (true) {
                actualizarEstadosSegunHora()
                kotlinx.coroutines.delay(30_000L)
            }
        }
    }

    private suspend fun actualizarEstadosSegunHora() {
        val ahora = Date().time

        val listaActual = _partidos.value
        val nueva = mutableListOf<Partido>()

        for (p in listaActual) {
            val fechaMillis = p.fecha?.toDate()?.time ?: Long.MAX_VALUE
            val jugadores = p.posiciones.count { it.isNotBlank() }

            val estadoDeseado = when {
                p.estado == "finalizado" -> "finalizado"
                fechaMillis <= ahora && jugadores < 4 -> "cancelado"
                fechaMillis <= ahora && jugadores == 4 -> "jugando"
                jugadores == 4 -> "listo"
                else -> "pendiente"
            }

            when (estadoDeseado) {
                "cancelado" -> {
                    if (p.id.isNotBlank()) HomeRepository.cancelarPorTiempo(p.id)
                }
                else -> {
                    if (estadoDeseado != p.estado && p.id.isNotBlank()) {
                        HomeRepository.actualizarEstado(p.id, estadoDeseado)
                        nueva.add(p.copy(estado = estadoDeseado))
                    } else nueva.add(p)
                }
            }
        }

        _partidos.value = nueva
    }

    fun solicitarUsuario(uid: String) {
        if (uid.isBlank()) return
        if (_usuarios.value.containsKey(uid)) return

        viewModelScope.launch {
            UsuarioRepository.obtenerUsuario(uid).getOrNull()?.let { usuario ->
                _usuarios.value = _usuarios.value.toMutableMap().apply {
                    put(uid, usuario)
                }
            }
        }
    }

    fun ocuparPosicion(partido: Partido, slot: Int, onMessage: (String) -> Unit) {
        val uid = currentUid

        if (partido.estado == "jugando") {
            onMessage("El partido ya está en juego")
            return
        }
        if (partido.estado == "finalizado") {
            onMessage("El partido ya ha finalizado")
            return
        }
        if (partido.posiciones.contains(uid)) return
        if (partido.posiciones[slot].isNotEmpty()) return

        viewModelScope.launch {
            val result = HomeRepository.ocuparPosicion(partido.id, slot, uid)
            onMessage(result.fold(
                onSuccess = { "Te uniste al partido" },
                onFailure = { it.message ?: "Error al unirse" }
            ))
        }
    }

    fun salirDePartido(partido: Partido, onMessage: (String) -> Unit) {
        if (partido.estado == "jugando") {
            onMessage("No puedes salir de un partido que está en juego")
            return
        }

        viewModelScope.launch {
            val res = HomeRepository.salirDePartido(partido.id, currentUid)
            onMessage(res.fold(
                onSuccess = { "Has salido del partido" },
                onFailure = { it.message ?: "No puedes salir" }
            ))
        }
    }

    fun borrarPartido(id: String, onMessage: (String) -> Unit) {
        viewModelScope.launch {
            val res = HomeRepository.borrarPartido(id, currentUid)
            onMessage(res.fold(
                onSuccess = { "Partido eliminado" },
                onFailure = { it.message ?: "Error al borrar partido" }
            ))
        }
    }

    fun finalizarPartido(
        partido: Partido,
        sets: List<Pair<String, String>>,
        onMessage: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUid != partido.creadorId) {
                onMessage("Solo el creador puede finalizar el partido")
                return@launch
            }
            if (partido.estado != "jugando") {
                onMessage("Solo puedes finalizar partidos en juego")
                return@launch
            }

            val listaSets = validarSets(sets, onMessage) ?: return@launch

            val res = HomeRepository.finalizarPartido(partido.id, listaSets)
            onMessage(res.fold(
                onSuccess = { "Partido finalizado" },
                onFailure = { it.message ?: "Error al finalizar partido" }
            ))
        }
    }

    private fun validarSets(
        raw: List<Pair<String, String>>,
        onMessage: (String) -> Unit
    ): List<SetResult>? {

        val sets = mutableListOf<SetResult>()

        raw.forEachIndexed { idx, (s1, s2) ->
            if (s1.isBlank() && s2.isBlank()) return@forEachIndexed
            if (s1.isBlank() || s2.isBlank()) {
                onMessage("Completa ambos marcadores en el set ${idx + 1}")
                return null
            }

            val j1 = s1.toIntOrNull()
            val j2 = s2.toIntOrNull()

            if (j1 == null || j2 == null) {
                onMessage("Marcadores inválidos en el set ${idx + 1}")
                return null
            }

            if (!esSetValido(j1, j2)) {
                onMessage("Marcador inválido en el set ${idx + 1}")
                return null
            }

            sets.add(SetResult(juegosEquipo1 = j1, juegosEquipo2 = j2))
        }

        if (sets.isEmpty()) {
            onMessage("Introduce al menos un set")
            return null
        }

        if (!validarLogicaSets(sets)) {
            onMessage("Resultado no válido (mejor de 3)")
            return null
        }

        return sets
    }

    private fun esSetValido(j1: Int, j2: Int): Boolean {
        if (j1 == j2) return false
        val max = max(j1, j2)
        val min = min(j1, j2)

        return when {
            max == 6 && min in 0..4 -> true
            max == 7 && (min == 5 || min == 6) -> true
            else -> false
        }
    }

    private fun validarLogicaSets(sets: List<SetResult>): Boolean {
        var w1 = 0
        var w2 = 0

        sets.forEachIndexed { idx, s ->
            if (s.juegosEquipo1 > s.juegosEquipo2) w1++ else w2++

            if ((w1 == 2 || w2 == 2) && idx < sets.lastIndex) return false
        }

        return w1 != w2 && w1 <= 2 && w2 <= 2
    }
}
