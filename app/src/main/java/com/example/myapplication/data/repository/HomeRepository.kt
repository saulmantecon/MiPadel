package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Partido
import com.example.myapplication.model.PartidoFinalizado
import com.example.myapplication.model.SetResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
object HomeRepository {

    private val db = FirebaseFirestoreManager.db
    private val partidosCollection = db.collection("partidos")
    private val usuariosCollection = db.collection("usuarios")
    private val finalizadosCollection = db.collection("partidos_finalizados")

    // -------- ESCUCHAR PARTIDOS --------
    fun escucharPartidos(): Flow<List<Partido>> = callbackFlow {
        val listener = partidosCollection
            .orderBy("fecha")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { it.toPartido() }
                    trySend(lista)
                }
            }

        awaitClose { listener.remove() }
    }

    private fun DocumentSnapshot.toPartido(): Partido? {
        val partido = this.toObject(Partido::class.java) ?: return null
        return partido.copy(id = this.id)
    }

    // -------- CREAR PARTIDO --------
    suspend fun crearPartido(partido: Partido): Result<Unit> {
        return try {
            val doc = partidosCollection.add(partido).await()
            doc.update("id", doc.id).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------- OCUPAR POSICIÓN --------
    suspend fun ocuparPosicion(partidoId: String, posicionIndex: Int, uid: String): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            if (posicionIndex !in 0 until partido.maxJugadores) {
                return Result.failure(Exception("Posición fuera de rango"))
            }

            val actuales = partido.posiciones.toMutableList()

            if (actuales.contains(uid)) return Result.success(Unit)
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

    // -------- SALIR DEL PARTIDO --------
    suspend fun salirDePartido(partidoId: String, uid: String): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            if (partido.creadorId == uid) {
                return Result.failure(Exception("El creador no puede salir del partido"))
            }

            if (!partido.posiciones.contains(uid)) {
                return Result.failure(Exception("No estás en este partido"))
            }

            val nuevas = partido.posiciones.map { if (it == uid) "" else it }

            doc.reference.update("posiciones", nuevas).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------- BORRAR PARTIDO MANUAL --------
    suspend fun borrarPartido(partidoId: String, uid: String): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            if (partido.creadorId != uid) {
                return Result.failure(Exception("Solo el creador puede eliminar el partido"))
            }

            partidosCollection.document(partidoId).delete().await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------- CANCELAR POR TIEMPO --------
    suspend fun cancelarPorTiempo(partidoId: String): Result<Unit> {
        return try {
            partidosCollection.document(partidoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------- ACTUALIZAR ESTADO --------
    suspend fun actualizarEstado(partidoId: String, nuevoEstado: String): Result<Unit> {
        return try {
            partidosCollection.document(partidoId)
                .update("estado", nuevoEstado)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // -------- FINALIZAR PARTIDO (TODO EN UNO) --------
    suspend fun finalizarPartido(partidoId: String, sets: List<SetResult>): Result<Unit> {
        return try {
            val doc = partidosCollection.document(partidoId).get().await()
            val partido = doc.toObject(Partido::class.java)
                ?: return Result.failure(Exception("Partido no encontrado"))

            val equipo1 = listOf(partido.posiciones[0], partido.posiciones[1])
            val equipo2 = listOf(partido.posiciones[2], partido.posiciones[3])

            // ---- 1) GUARDAR PARTIDO FINALIZADO ----
            val finalizado = PartidoFinalizado(
                id = partido.id,
                fecha = partido.fecha,
                ubicacion = partido.ubicacion,
                posiciones = partido.posiciones,
                sets = sets
            )

            PartidoFinalizadoRepository.guardar(finalizado)


            // ---- 2) ACTUALIZAR ESTADÍSTICAS ----
            val wins1 = sets.count { it.juegosEquipo1 > it.juegosEquipo2 }
            val wins2 = sets.count { it.juegosEquipo1 < it.juegosEquipo2 }
            val ganador_es_equipo1 = wins1 > wins2

            val batch = db.batch()

            (equipo1 + equipo2).forEach { uid ->
                val ref = usuariosCollection.document(uid)
                batch.update(ref, "partidosJugados", FieldValue.increment(1))

                val esGanador =
                    (ganador_es_equipo1 && uid in equipo1) ||
                            (!ganador_es_equipo1 && uid in equipo2)

                if (esGanador)
                    batch.update(ref, "partidosGanados", FieldValue.increment(1))
                else
                    batch.update(ref, "partidosPerdidos", FieldValue.increment(1))
            }

            batch.commit().await()


            // ---- 3) BORRAR PARTIDO DE LA COLECCIÓN PRINCIPAL ----
            partidosCollection.document(partidoId).delete().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
