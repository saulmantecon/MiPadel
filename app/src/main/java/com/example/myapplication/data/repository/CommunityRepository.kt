package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Amistad
import com.example.myapplication.model.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await


object CommunityRepository {

    private val db = FirebaseFirestoreManager.db
    private val amistadesCollection = db.collection("amistades")
    private val usuariosCollection = db.collection("usuarios")


    suspend fun enviarSolicitud(fromUid: String, toUid: String): Result<Unit> {
        return try {

            // 1) Primero miramos si ya existe una relación
            val query = amistadesCollection
                .whereIn("user1", listOf(fromUid, toUid))
                .whereIn("user2", listOf(fromUid, toUid))
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents.first()
                val estadoActual = doc.getString("estado") ?: "pendiente"

                return when (estadoActual) {
                    "pendiente" ->
                        Result.failure(Exception("Ya hay una solicitud pendiente."))

                    "aceptado" ->
                        Result.failure(Exception("Ya sois amigos."))

                    "rechazado" -> {
                        // REENVIAR: reseteamos el estado
                        doc.reference.update(
                            mapOf(
                                "estado" to "pendiente",
                                "enviadoPor" to fromUid,
                                "timestamp" to Timestamp.now()
                            )
                        ).await()

                        Result.success(Unit)
                    }
                    "eliminado" -> {
                        // Reutilizar documento eliminando
                        doc.reference.update(
                            mapOf(
                                "estado" to "pendiente",
                                "enviadoPor" to fromUid,
                                "timestamp" to Timestamp.now()
                            )
                        ).await()

                        Result.success(Unit)
                    }

                    else -> Result.failure(Exception("Estado desconocido"))
                }
            }

            // 2) NO existe → crear solicitud nueva
            val data = mapOf(
                "user1" to fromUid,
                "user2" to toUid,
                "estado" to "pendiente",
                "enviadoPor" to fromUid,
                "timestamp" to Timestamp.now()
            )

            amistadesCollection.add(data).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    // 2. OBTENER SOLICITUDES RECIBIDAS
    suspend fun obtenerSolicitudesRecibidas(uid: String): Result<List<Amistad>> {
        return try {
            val snapshot = amistadesCollection
                .whereEqualTo("user2", uid)
                .whereEqualTo("estado", "pendiente")
                .get()
                .await()

            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Amistad::class.java)?.copy(docId = doc.id)
            }

            Result.success(solicitudes)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // 4. ACEPTAR SOLICITUD
    suspend fun aceptarSolicitud(docId: String, fromUid: String, toUid: String): Result<Unit> {
        return try {

            // 1. Cambiar estado
            amistadesCollection.document(docId)
                .update("estado", "aceptado")
                .await()

            // 2. Añadir en ambos usuarios
            usuariosCollection.document(fromUid)
                .update("amigos", FieldValue.arrayUnion(toUid))
                .await()

            usuariosCollection.document(toUid)
                .update("amigos", FieldValue.arrayUnion(fromUid))
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // 5. RECHAZAR SOLICITUD
    suspend fun rechazarSolicitud(docId: String): Result<Unit> {
        return try {
            amistadesCollection.document(docId)
                .update("estado", "rechazado")
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // 6. OBTENER LISTA DE AMIGOS (usuarios aceptados)
    suspend fun obtenerAmigos(uid: String): Result<List<Usuario>> {
        return try {
            val userDoc = usuariosCollection.document(uid).get().await()
            val amigos = userDoc.get("amigos") as? List<*> ?: emptyList<String>()

            if (amigos.isEmpty()) return Result.success(emptyList())

            val snapshot = usuariosCollection
                .whereIn("uid", amigos)
                .get()
                .await()

            val lista = snapshot.toObjects(Usuario::class.java)

            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarAmigo(uid1: String, uid2: String): Result<Unit> {
        return try {

            // 1. Eliminar uid2 de amigos de uid1
            usuariosCollection.document(uid1)
                .update("amigos", FieldValue.arrayRemove(uid2))
                .await()

            // 2. Eliminar uid1 de amigos de uid2
            usuariosCollection.document(uid2)
                .update("amigos", FieldValue.arrayRemove(uid1))
                .await()

            // 3. Marcar relación como "eliminado"
            val query = amistadesCollection
                .whereIn("user1", listOf(uid1, uid2))
                .whereIn("user2", listOf(uid1, uid2))
                .limit(1)
                .get()
                .await()

            if (!query.isEmpty) {
                query.documents.first().reference.update("estado", "eliminado").await()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}