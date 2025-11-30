package com.example.myapplication.model
//Inicializar atributos con valores por defecto o null para que FireStore pueda serializar/deserializar la dataclass sin errores
data class Partido(
    val id: String = "",
    val creadorId: String = "",
    val ubicacion: String = "",
    val nivel: Double = 0.0,
    val fecha: com.google.firebase.Timestamp? = null,
    val posiciones: List<String> = listOf("", "", "", ""),
    val maxJugadores: Int = 4,
    val estado: String = "pendiente",
    val sets: List<SetResult> = emptyList(),
    val ganador: Int = 0
)
