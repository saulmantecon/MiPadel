package com.example.myapplication.model

import com.google.firebase.Timestamp

data class Amistad(
    val docId: String = "",        // ID del documento en Firestore
    val user1: String = "",        // UID del usuario A
    val user2: String = "",        // UID del usuario B
    val estado: String = "pendiente", // "pendiente", "aceptado", "rechazado"
    val enviadoPor: String = "",   // UID del que envió la solicitud
    val timestamp: Timestamp? = null
)
