package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Partido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await




object HomeRepository {

    private val db = FirebaseFirestoreManager.db
    private val partidosCollection = db.collection("partidos")

    // Escuchar todos los partidos en tiempo real
    fun escucharPartidos(): Flow<List<Partido>> = callbackFlow {
        val listener = partidosCollection
            .orderBy("fecha") // opcional, por fecha
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        doc.toPartido()
                    }
                    trySend(lista)
                }
            }

        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toPartido(): Partido? {
        val partido = this.toObject(Partido::class.java) ?: return null
        return partido.copy(id = this.id)
    }

    // Crear partido
    suspend fun crearPartido(partido: Partido): Result<Unit> {
        return try {
            val docRef = partidosCollection.add(partido).await()
            docRef.update("id", docRef.id).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ocupar una posición concreta (0..3)
    suspend fun ocuparPosicion(partidoId: String, posicionIndex: Int, uid: String): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            if (posicionIndex !in 0 until partido.maxJugadores) {
                return Result.failure(Exception("Posición fuera de rango"))
            }

            val actuales = partido.posiciones.toMutableList()

            // si ya estás en alguna posición, no te volvemos a meter
            if (actuales.contains(uid)) {
                return Result.success(Unit)
            }

            // si la posición ya está ocupada por otro
            if (actuales[posicionIndex].isNotEmpty()) {
                return Result.failure(Exception("Esa posición ya está ocupada"))
            }

            actuales[posicionIndex] = uid

            doc.reference.update("posiciones", actuales).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Salir del partido (vacía cualquier posición donde esté el uid)
    suspend fun salirDePartido(partidoId: String, uid: String): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            // el creador no puede salir
            if (partido.creadorId == uid) {
                return Result.failure(Exception("El creador no puede salir del partido"))
            }

            val nuevas = partido.posiciones.map { posUid ->
                if (posUid == uid) "" else posUid
            }

            // si no estaba en ninguna posición
            if (!partido.posiciones.contains(uid)) {
                return Result.failure(Exception("No estás en este partido"))
            }

            doc.reference.update("posiciones", nuevas).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Borrar partido
    suspend fun borrarPartido(partidoId: String, uidSolicitante: String): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            if (partido.creadorId != uidSolicitante) {
                return Result.failure(Exception("Solo el creador puede eliminar el partido"))
            }

            partidosCollection.document(partidoId).delete().await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}