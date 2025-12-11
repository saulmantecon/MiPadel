package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.PartidoFinalizado
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object PartidoFinalizadoRepository {

    private val db = FirebaseFirestoreManager.db
    private val col = db.collection("partidos_finalizados")

    //GUARDAR PARTIDO FINALIZADO
    suspend fun guardar(partido: PartidoFinalizado): Result<Unit> {
        return try {
            val doc = col.document()   // ← ID NUEVO
            val realId = doc.id

            // Guardamos el id dentro del modelo
            val finalConId = partido.copy(id = realId)

            doc.set(finalConId).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    //OBTENER HISTORIAL DEL USUARIO
    suspend fun obtenerPartidosDeUsuario(uid: String): Result<List<PartidoFinalizado>> {
        return try {
            val snap = col
                .whereArrayContains("posiciones", uid)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val lista = snap.documents.mapNotNull { doc ->
                doc.toObject(PartidoFinalizado::class.java)?.copy(id = doc.id)
            }

            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}