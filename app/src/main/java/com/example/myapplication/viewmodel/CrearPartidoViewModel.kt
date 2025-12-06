package com.example.myapplication.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.HomeRepository
import com.example.myapplication.model.Partido
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class CrearPartidoViewModel : ViewModel() {

    private val _ubicacion = MutableStateFlow("")
    val ubicacion = _ubicacion.asStateFlow()

    private val _fecha = MutableStateFlow<Timestamp?>(null)
    val fecha = _fecha.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private val uidCreador = CurrentUserManager.getUsuario()?.uid ?: ""

    fun setUbicacion(texto: String) {
        _ubicacion.value = texto
    }

    fun setFecha(timestamp: Timestamp) {
        _fecha.value = timestamp
    }

    fun crearPartido() {
        val ubic = _ubicacion.value.trim()
        val fechaPartido = _fecha.value

        if (ubic.isEmpty() || fechaPartido == null) {
            _mensaje.value = "Ubicación y fecha son obligatorias"
            return
        }

        viewModelScope.launch {
            val ahora = Date()
            if (fechaPartido.toDate().before(ahora)) {
                _mensaje.value = "No puedes crear un partido en el pasado"
                return@launch
            }

            val conflicto = HomeRepository.existePartidoSolapado(ubic, fechaPartido)
            val hay = conflicto.getOrElse {
                _mensaje.value = "Error al comprobar disponibilidad"
                return@launch
            }

            if (hay) {
                _mensaje.value = "Ya hay un partido cerca de esa hora"
                return@launch
            }

            _loading.value = true

            val partido = Partido(
                id = "",
                creadorId = uidCreador,
                ubicacion = ubic,
                nivel = 0.0,
                fecha = fechaPartido,
                posiciones = listOf(uidCreador, "", "", ""),
                maxJugadores = 4,
                estado = "pendiente",
                sets = emptyList()
            )

            val result = HomeRepository.crearPartido(partido)

            _mensaje.value = result.fold(
                onSuccess = { "Partido creado correctamente" },
                onFailure = { it.message ?: "Error al crear partido" }
            )

            _loading.value = false
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }

    fun resetForm() {
        _ubicacion.value = ""
        _fecha.value = null
    }
}