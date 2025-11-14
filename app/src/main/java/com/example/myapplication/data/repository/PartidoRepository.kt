package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Partido
import kotlinx.coroutines.tasks.await

object PartidoRepository {

    private val db = FirebaseFirestoreManager.db
    private val partidosCollection = db.collection("partidos")

    suspend fun obtenerPartidosDeUsuario(uid: String): List<Partido> {
        return try {
            val snapshot = partidosCollection
                .whereArrayContains("jugadores", uid)
                .get()
                .await()

            snapshot.toObjects(Partido::class.java)

        } catch (_: Exception) {
            emptyList()
        }
    }
}
