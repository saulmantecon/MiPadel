package com.example.myapplication.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Partido
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.repository.HomeRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class CrearPartidoViewModel : ViewModel() {

    private val _ubicacion = MutableStateFlow("")
    val ubicacion = _ubicacion.asStateFlow()

    private val _fecha = MutableStateFlow<Timestamp?>(null)
    val fecha = _fecha.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private val uidActual = CurrentUserManager.getUsuario()?.uid ?: ""

    fun setUbicacion(text: String) {
        _ubicacion.value = text
    }

    fun setFecha(ts: Timestamp) {
        _fecha.value = ts
    }

    fun crearPartido() {
        if (ubicacion.value.isBlank() || fecha.value == null) {
            _mensaje.value = "Ubicación y fecha son obligatorias"
            return
        }

        viewModelScope.launch {
            _loading.value = true

            val partido = Partido(
                id = "",
                creadorId = uidActual,
                ubicacion = ubicacion.value.trim(),
                nivel = 0.0,
                fecha = fecha.value,
                jugadores = listOf(uidActual),
                maxJugadores = 4
            )

            val result = HomeRepository.crearPartido(partido)
            _mensaje.value = result.fold(
                { "Partido creado correctamente" },
                { it.message ?: "Error al crear partido" }
            )

            _loading.value = false
        }
    }

    fun resetForm() {
        _ubicacion.value = ""
        _fecha.value = null
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}

