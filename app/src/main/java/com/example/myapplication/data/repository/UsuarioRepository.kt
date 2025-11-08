package com.example.myapplication.data.repository

import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.FirebaseAuthManager
import com.example.myapplication.data.FirebaseAuthManager.auth
import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Usuario
import com.google.firebase.Timestamp
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
    suspend fun registrarUsuario(nombre: String, email: String, password: String): Result<Usuario> {
        return try {
            // 1️. Crear usuario en Auth (email único)
            val authResult = auth.createUserWithEmailAndPassword(email.trim(),password).await()

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
                fechaRegistro = Timestamp.now()
            )

            usuariosCollection.document(uid).set(usuario).await()

            Result.success(usuario)

        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("Ese correo ya está registrado"))

        } catch (e: Exception) {
            // rollback si Auth se creó pero Firestore falló
            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) { }
            Result.failure(e)
        }
    }

    suspend fun loginUsuario(email: String, password: String): Result<Usuario> {
        return try {
            // 1️. Autenticamos con FirebaseAuth
            val authResult = auth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()

            val uid = authResult.user?.uid
                ?: return Result.failure(IllegalStateException("UID no encontrado"))

            //retorna directamente Result<Usuario>
            obtenerUsuario(uid)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsuario(usuario: Usuario) {
        try {
            // Obtenemos el usuario actual guardado en memoria
            val usuarioActual = CurrentUserManager.getUsuario()
                ?: throw IllegalStateException("No hay usuario cargado en memoria")

            val docRef = usuariosCollection.document(usuario.uid)

            // Comparamos solo con el usuario actual en memoria
            val camposModificados = mutableMapOf<String, Any>()

            if (usuarioActual.username != usuario.username) camposModificados["username"] = usuario.username
            if (usuarioActual.nombre != usuario.nombre) camposModificados["nombre"] = usuario.nombre
            if (usuarioActual.nivel != usuario.nivel) camposModificados["nivel"] = usuario.nivel
            if (usuarioActual.fotoPerfilUrl != usuario.fotoPerfilUrl) camposModificados["fotoPerfilUrl"] =
                usuario.fotoPerfilUrl as Any
            if (usuarioActual.amigos != usuario.amigos) camposModificados["amigos"] = usuario.amigos
            if (usuarioActual.partidosJugados != usuario.partidosJugados) camposModificados["partidosJugados"] = usuario.partidosJugados
            if (usuarioActual.partidosGanados != usuario.partidosGanados) camposModificados["partidosGanados"] = usuario.partidosGanados
            if (usuarioActual.partidosPerdidos != usuario.partidosPerdidos) camposModificados["partidosPerdidos"] = usuario.partidosPerdidos

            if (camposModificados.isEmpty()) return // no hay nada que actualizar

            // Actualiza solo los campos modificados
            docRef.update(camposModificados).await()

            // Actualiza también el usuario en memoria
            CurrentUserManager.setUsuario(usuario)

        } catch (e: Exception) {
            throw e
        }
    }


    /**
     * Obtener un usuario por su UID.
     */
    suspend fun obtenerUsuario(uid: String): Result<Usuario> {
        return try {
            val snapshot = usuariosCollection.document(uid).get().await()
            val usuario = snapshot.toObject(Usuario::class.java) ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
