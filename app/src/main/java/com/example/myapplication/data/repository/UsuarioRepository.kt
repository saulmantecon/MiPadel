package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Usuario
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await


object UsuarioRepository {

    private val db = FirebaseFirestoreManager.db
    private val usuariosCollection = db.collection("usuarios")

    /**
     * Crear o actualizar un usuario.
     * Si el documento no existe, lo crea; si existe, lo sobreescribe.
     */
    suspend fun guardarUsuario(usuario: Usuario) {
        usuariosCollection.document(usuario.uid).set(usuario).await()
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
