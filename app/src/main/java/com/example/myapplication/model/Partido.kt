package com.example.myapplication.model
//Inicializar atributos con valores por defecto o null para que FireStore pueda serializar/deserializar la dataclass sin errores
data class Partido(
    val id: String = "",
    val creadorId: String = "",
    val ubicacion: String = "",
    val nivel: Double = 0.0,
    val fecha: com.google.firebase.Timestamp? = null,
    val jugadores: List<String> = emptyList(),
    val maxJugadores: Int = 4
)
