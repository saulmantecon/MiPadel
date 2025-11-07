package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseAuthManager
import com.example.myapplication.data.FirebaseAuthManager.auth
import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await


object UsuarioRepository {

    private val db = FirebaseFirestoreManager.db
    private val usuariosCollection = db.collection("usuarios")


    /**
     * Registra un usuario:
     * 1) Crea cuenta en Auth (email único).
     * 2) Crea doc de perfil en Firestore con create() (no sobreescribe).
     * 3) Si falla Firestore, hace rollback borrando la cuenta Auth recién creada.
     */
    suspend fun registrarUsuario(nombre: String, email: String, password: String): Result<Unit> {
        return try {
            // 1️. Crear usuario en Auth (email único)
            val authResult = FirebaseAuthManager.auth.createUserWithEmailAndPassword(email.trim(),password).await()

            val uid = authResult.user?.uid
                ?: return Result.failure(IllegalStateException("No se pudo obtener UID"))

            // 2️. Crear documento del usuario en Firestore
            val usuario = Usuario(
                uid = uid,
                nombre = nombre.trim(),
                email = email.trim().lowercase(),
                nivel = 1,
                amigos = emptyList(),
                partidosJugados = 0,
                partidosGanados = 0,
                partidosPerdidos = 0,
                fechaRegistro = com.google.firebase.Timestamp.now()
            )

            usuariosCollection.document(uid).set(usuario).await()

            Result.success(Unit)

        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("Ese correo ya está registrado"))

        } catch (e: Exception) {
            // rollback si Auth se creó pero Firestore falló
            try {
                FirebaseAuthManager.auth.currentUser?.delete()?.await()
            } catch (_: Exception) { }
            Result.failure(e)
        }
    }


    /**
     * Obtener un usuario por su UID.
     */
    suspend fun obtenerUsuario(uid: String): Usuario? {
        val snapshot = usuariosCollection.document(uid).get().await()
        return snapshot.toObject<Usuario>()
    }

    /**
     * Eliminar un usuario por su UID.
     */
    suspend fun eliminarUsuario(uid: String) {
        usuariosCollection.document(uid).delete().await()
    }

    /**
     * Obtener todos los usuarios (por ejemplo, para buscador o lista de amigos).
     */
    suspend fun obtenerTodos(): List<Usuario> {
        val snapshot = usuariosCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject<Usuario>() }
    }

    /**
     * Actualizar campos individuales del usuario.
     * Ejemplo: actualizar nivel o estadísticas.
     */
    suspend fun actualizarCampo(uid: String, campo: String, valor: Any) {
        usuariosCollection.document(uid).update(campo, valor).await()
    }
}
