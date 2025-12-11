package com.example.myapplication.data.repository

import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.FirebaseAuthManager.auth
import com.example.myapplication.data.FirebaseFirestoreManager
import com.example.myapplication.model.Usuario
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthUserCollisionException
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
    suspend fun registrarUsuario(
        username: String,
        nombre: String,
        email: String,
        password: String
    ): Result<Usuario> {
        return try {
            // Normalizamos datos de entrada
            val usernameClean = username.trim()
            val nombreClean = nombre.trim()
            val emailClean = email.trim().lowercase()

            //Comprobar si el nombre de usuario ya existe
            val usernameQuery = usuariosCollection
                .whereEqualTo("username", usernameClean)
                .limit(1)
                .get()
                .await()

            if (!usernameQuery.isEmpty) {
                // Ya hay un usuario con ese username
                return Result.failure(IllegalArgumentException("Ese nombre de usuario ya está en uso"))
            }

            //Crear usuario en Auth (email único)
            val authResult = auth.createUserWithEmailAndPassword(emailClean, password).await()

            val uid = authResult.user?.uid
                ?: return Result.failure(IllegalStateException("No se pudo obtener UID"))

            //Crear documento del usuario en Firestore
            val usuario = Usuario(
                uid = uid,
                nombre = nombreClean,
                email = emailClean,
                username = usernameClean,
                nivel = 1,
                amigos = emptyList(),
                partidosJugados = 0,
                partidosGanados = 0,
                partidosPerdidos = 0,
                fechaRegistro = Timestamp.now()
            )

            usuariosCollection.document(uid).set(usuario).await()

            Result.success(usuario)

        } catch (_: FirebaseAuthUserCollisionException) {
            //Email ya registrado en Firebase Auth
            Result.failure(IllegalArgumentException("Ese correo ya está registrado"))

        } catch (e: Exception) {
            //rollback si Auth se creó pero Firestore falló
            try {
                auth.currentUser?.delete()?.await()
            } catch (_: Exception) { }

            Result.failure(e)
        }
    }


    suspend fun loginUsuario(email: String, password: String): Result<Usuario> {
        return try {
            //Autenticamos con FirebaseAuth
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
    /**
     * Actualiza el documento del usuario en Firestore.
     * Retorna Result<Unit> indicando éxito o error.
     */
    suspend fun updateUsuario(usuario: Usuario): Result<Unit> {
        return try {
            //Usuario actual en memoria (antes de editar)
            val usuarioActual = CurrentUserManager.getUsuario()
                ?: return Result.failure(IllegalStateException("No hay usuario cargado en memoria"))

            val docRef = usuariosCollection.document(usuario.uid)

            //Normalizar  campos que pueden venir con espacios
            val usernameNuevo = usuario.username.trim()
            val nombreNuevo = usuario.nombre.trim()

            //1) Si ha cambiado el username, comprobamos que no esté repetido
            if (usuarioActual.username != usernameNuevo) {
                val consulta = usuariosCollection
                    .whereEqualTo("username", usernameNuevo)
                    .limit(1)
                    .get()
                    .await()

                if (!consulta.isEmpty) {
                    val doc = consulta.documents.first()

                    // Si el documento encontrado NO es el propio usuario => nombre ya usado
                    if (doc.id != usuarioActual.uid) {
                        return Result.failure(
                            IllegalArgumentException("Ese nombre de usuario ya está en uso")
                        )
                    }
                }
            }

            //2) Detectamos los campos modificados
            val camposModificados = mutableMapOf<String, Any>()

            if (usuarioActual.username != usernameNuevo) {
                camposModificados["username"] = usernameNuevo
            }
            if (usuarioActual.nombre != nombreNuevo) {
                camposModificados["nombre"] = nombreNuevo
            }
            if (usuarioActual.nivel != usuario.nivel) {
                camposModificados["nivel"] = usuario.nivel
            }
            if (usuarioActual.fotoPerfilUrl != usuario.fotoPerfilUrl && usuario.fotoPerfilUrl != null) {
                camposModificados["fotoPerfilUrl"] = usuario.fotoPerfilUrl
            }
            if (usuarioActual.amigos != usuario.amigos) {
                camposModificados["amigos"] = usuario.amigos
            }
            if (usuarioActual.partidosJugados != usuario.partidosJugados) {
                camposModificados["partidosJugados"] = usuario.partidosJugados
            }
            if (usuarioActual.partidosGanados != usuario.partidosGanados) {
                camposModificados["partidosGanados"] = usuario.partidosGanados
            }
            if (usuarioActual.partidosPerdidos != usuario.partidosPerdidos) {
                camposModificados["partidosPerdidos"] = usuario.partidosPerdidos
            }

            //3) Si no hay cambios, no tocamos Firestore
            if (camposModificados.isEmpty()) {
                return Result.success(Unit)
            }

            //4) Actualizamos solo los campos modificados en Firestore
            docRef.update(camposModificados).await()

            //5) Actualizamos el usuario en memoria con los datos limpios
            val usuarioActualizado = usuario.copy(
                username = usernameNuevo,
                nombre = nombreNuevo
            )
            CurrentUserManager.setUsuario(usuarioActualizado)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * Obtener un usuario por su UID.
     */
    suspend fun obtenerUsuario(uid: String): Result<Usuario> {
        return try {
            val snapshot = usuariosCollection.document(uid).get().await()
            val usuario = snapshot.toObject(Usuario::class.java) ?: return Result.failure(
                IllegalStateException("Usuario no encontrado")
            )
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // BUSCAR USUARIOS POR USERNAME
    suspend fun buscarUsuarios(query: String): Result<List<Usuario>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val snapshot = usuariosCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .await()

            val lista = snapshot.toObjects(Usuario::class.java)

            Result.success(lista)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


