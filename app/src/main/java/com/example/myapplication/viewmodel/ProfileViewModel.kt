package com.example.myapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.ImageUploader
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Usuario
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    // Observa el usuario actual desde el CurrentUserManager
    val usuario = CurrentUserManager.usuario

    /**
     * Actualiza los datos del usuario:
     * - Si hay una nueva imagen, primero la sube a ImgBB.
     * - Luego actualiza el documento en Firestore.
     * - Finalmente actualiza el flujo global (StateFlow).
     */
    suspend fun updateUsuarioCompleto(
        context: Context,
        usuario: Usuario,
        nuevaImagenUri: Uri?
    ): String {
        return try {
            var usuarioActualizado = usuario

            // Si el usuario ha elegido una nueva imagen, la subimos
            if (nuevaImagenUri != null) {
                val nuevaUrl = ImageUploader.uploadImage(context, nuevaImagenUri)
                if (nuevaUrl != null) {
                    usuarioActualizado = usuarioActualizado.copy(fotoPerfilUrl = nuevaUrl)
                } else {
                    return "Error al subir la imagen"
                }
            }

            // Actualizamos Firestore
            val result = UsuarioRepository.updateUsuario(usuarioActualizado)
            result.fold(
                onSuccess = {
                    CurrentUserManager.setUsuario(usuarioActualizado)
                    "Perfil actualizado correctamente"
                },
                onFailure = { e ->
                    "Error al actualizar usuario: ${e.message ?: "desconocido"}"
                }
            )
        } catch (e: Exception) {
            "Error al actualizar usuario: ${e.message ?: "desconocido"}"
        }
    }
}