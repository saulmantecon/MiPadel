package com.example.myapplication.model

import com.google.firebase.Timestamp

data class PartidoFinalizado(
    val id: String = "",
    val creadorId: String = "",
    val ubicacion: String = "",
    val fecha: Timestamp? = null,
    val posiciones: List<String> = listOf("", "", "", ""),
    val sets: List<SetResult> = emptyList(),
    val ganador: Int = 0 // 1 = equipo1, 2 = equipo2
)
