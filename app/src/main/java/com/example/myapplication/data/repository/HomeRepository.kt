package com.example.myapplication.data.repository

import com.example.myapplication.model.Partido
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await



object HomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val partidosCollection = db.collection("partidos")

    // ---------------------------------------------------------
    // 1. ESCUCHAR PARTIDOS EN TIEMPO REAL
    // ---------------------------------------------------------
    fun escucharPartidos(): Flow<List<Partido>> = callbackFlow {
        val listener = partidosCollection
            .orderBy("fecha")
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val lista = snap.documents.mapNotNull { doc ->
                    doc.toObject(Partido::class.java)?.copy(id = doc.id)
                }

                trySend(lista)
            }

        awaitClose { listener.remove() }
    }


    // ---------------------------------------------------------
    // 2. CREAR PARTIDO (el creador es el primer jugador)
    // ---------------------------------------------------------
    suspend fun crearPartido(partido: Partido): Result<Unit> {
        return try {
            val data = mapOf(
                "creadorId" to partido.creadorId,
                "ubicacion" to partido.ubicacion,
                "nivel" to partido.nivel,
                "fecha" to partido.fecha,
                "jugadores" to partido.jugadores,
                "maxJugadores" to partido.maxJugadores,
                "estado" to "pendiente" // pendiente / completo / jugando / finalizado
            )

            partidosCollection.add(data).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ---------------------------------------------------------
    // 3. OCUPAR HUECO EN PARTIDO
    // slot = 0..3 → posición dentro de jugadores
    // ---------------------------------------------------------
    suspend fun ocuparHueco(partidoId: String, slot: Int, uid: String): Result<Unit> {
        return try {
            val ref = partidosCollection.document(partidoId)
            val snap = ref.get().await()

            val partido = snap.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no existe"))

            val jugadores = partido.jugadores.toMutableList()

            // si ya está dentro, no hace falta meterlo
            if (jugadores.contains(uid)) return Result.success(Unit)

            // si el hueco está fuera de rango
            if (slot < 0 || slot >= partido.maxJugadores)
                return Result.failure(Exception("Slot fuera de rango"))

            // si el slot ya está ocupado
            if (jugadores.size > slot && jugadores[slot].isNotEmpty())
                return Result.failure(Exception("Slot ocupado"))

            // ampliar lista si fuese necesario
            while (jugadores.size <= slot) {
                jugadores.add("")
            }

            // meter al usuario
            jugadores[slot] = uid

            // filtrar huecos vacíos al final
            val compactado = jugadores.filter { it.isNotEmpty() }

            ref.update("jugadores", compactado).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ---------------------------------------------------------
    // 4. SALIR DE UN HUECO
    // ---------------------------------------------------------
    suspend fun salirDeHueco(partidoId: String, uid: String): Result<Unit> {
        return try {
            val ref = partidosCollection.document(partidoId)
            val snap = ref.get().await()

            val partido = snap.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            // No se puede salir si eres el creador
            if (partido.creadorId == uid)
                return Result.failure(Exception("El creador no puede salir del partido"))

            val nuevaLista = partido.jugadores.filter { it != uid }

            ref.update("jugadores", nuevaLista).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ---------------------------------------------------------
    // 5. BORRAR PARTIDO (solo creador)
    // ---------------------------------------------------------
    suspend fun borrarPartido(partidoId: String, uid: String): Result<Unit> {
        return try {
            val ref = partidosCollection.document(partidoId)
            val snap = ref.get().await()

            val partido = snap.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            if (partido.creadorId != uid)
                return Result.failure(Exception("No puedes borrar un partido que no creaste"))

            ref.delete().await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
