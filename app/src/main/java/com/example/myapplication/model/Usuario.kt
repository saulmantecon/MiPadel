package com.example.myapplication.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val nivel: Int = 0,
    val amigos: List<String> = emptyList(),
    val fotoPerfilUrl: String? = null,
    val partidosJugados: Int = 0,
    val partidosGanados: Int = 0,
    val partidosPerdidos: Int = 0,
    val fechaRegistro: com.google.firebase.Timestamp? = null
)
