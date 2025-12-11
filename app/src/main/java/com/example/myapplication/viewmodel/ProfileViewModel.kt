package com.example.myapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.ImageUploader
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.Usuario

/**
 * ViewModel para gestionar la edición del perfil.
 * - Sube la foto si existe
 * - Actualiza el documento en Firestore
 * - Refresca el usuario global en CurrentUserManager
 */
class ProfileViewModel : ViewModel() {

    // Usuario global observable por toda la app
    val usuario = CurrentUserManager.usuario

    /**
     * Actualiza el usuario completo en Firestore.
     * Si hay imagen nueva -> la sube primero.
     */
    suspend fun updateUsuarioCompleto(
        context: Context,
        usuario: Usuario,
        nuevaImagenUri: Uri?
    ): String {
        return try {
            var usuarioActualizado = usuario

            // Subir imagen si el usuario escogió una nueva
            if (nuevaImagenUri != null) {
                val nuevaUrl = ImageUploader.uploadImage(context, nuevaImagenUri)

                if (nuevaUrl == null) {
                    return "Error al subir la imagen"
                }

                usuarioActualizado = usuarioActualizado.copy(fotoPerfilUrl = nuevaUrl)
            }

            // Guardar cambios en Firestore
            val result = UsuarioRepository.updateUsuario(usuarioActualizado)

            result.fold(
                onSuccess = {
                    // También actualizar usuario global
                    CurrentUserManager.setUsuario(usuarioActualizado)
                    "Perfil actualizado correctamente"
                },
                onFailure = { e ->
                    "Error al actualizar usuario: ${e.message}"
                }
            )
        } catch (e: Exception) {
            "Error inesperado: ${e.message}"
        }
    }
}
