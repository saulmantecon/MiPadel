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

/**
 * ViewModel responsable de gestionar la creación de partidos.
 *
 * - Mantiene el estado del formulario (ubicación y fecha).
 * - Valida los datos antes de crear el partido.
 * - Llama al repositorio y expone mensajes para UI.
 *
 * Esta capa permite que la pantalla sea sencilla y reactiva.
 */
class CrearPartidoViewModel : ViewModel() {

    //Campo de texto donde el usuario introduce la ubicación.
    private val _ubicacion = MutableStateFlow("")
    val ubicacion = _ubicacion.asStateFlow()

    //Fecha seleccionada mediante DatePicker + TimePicker.
    private val _fecha = MutableStateFlow<Timestamp?>(null)
    val fecha = _fecha.asStateFlow()

    //Estado de carga durante la creación del partido.
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    /**
     * Mensaje que se mostrará en la UI mediante Snackbar.
     * Es null cuando no hay mensaje pendiente.
     */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    //UID del usuario actual, utilizado como creador del partido.
    private val uidCreador = CurrentUserManager.getUsuario()?.uid ?: ""

    fun setUbicacion(texto: String) {
        _ubicacion.value = texto
    }

    fun setFecha(timestamp: Timestamp) {
        _fecha.value = timestamp
    }

    /**
     * Acción principal de este ViewModel:
     * valida el formulario -> comprueba solape -> crea el partido.
     */
    fun crearPartido() {
        val ubic = _ubicacion.value.trim()
        val fechaPartido = _fecha.value

        // Validaciones básicas antes de proceder
        if (ubic.isEmpty() || fechaPartido == null) {
            _mensaje.value = "Ubicación y fecha son obligatorias"
            return
        }

        viewModelScope.launch {

            // El usuario no puede crear un partido en el pasado
            if (fechaPartido.toDate().before(Date())) {
                _mensaje.value = "No puedes crear un partido en el pasado"
                return@launch
            }

            // Comprobar si existe algún partido cerca de la misma hora / lugar
            val conflicto = HomeRepository.existePartidoSolapado(ubic, fechaPartido)
            val haySolapado = conflicto.getOrElse {
                _mensaje.value = "Error al comprobar disponibilidad"
                return@launch
            }

            if (haySolapado) {
                _mensaje.value = "Ya hay un partido cerca de esa hora"
                return@launch
            }

            // Inicia loading
            _loading.value = true

            // Construimos el objeto Partido
            val partido = Partido(
                id = "",
                creadorId = uidCreador,
                ubicacion = ubic,
                nivel = 0.0, // nivel futuro
                fecha = fechaPartido,
                posiciones = listOf(uidCreador, "", "", ""), // creador en posición 0
                maxJugadores = 4,
                estado = "pendiente",
                sets = emptyList()
            )

            // Guardar en Firestore
            val result = HomeRepository.crearPartido(partido)

            _mensaje.value = result.fold(
                onSuccess = { "Partido creado correctamente" },
                onFailure = { it.message ?: "Error al crear partido" }
            )

            // Finalizamos loading
            _loading.value = false
        }
    }

    /** Limpia el mensaje después de mostrar el Snackbar. */
    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /**
     * Se ejecuta cuando el usuario cierra el sheet o al crear partido.
     * Reinicia el formulario a su estado inicial.
     */
    fun resetForm() {
        _ubicacion.value = ""
        _fecha.value = null
    }
}
