package com.example.myapplication.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Subir imágenes a ImgBB usando una petición HTTP multipart.
 */
object ImageUploader {

    private const val API_KEY = "c9879b31ac20e1e16727cc68a4c93db3"
    private const val TAG = "ImageUploader"

    /**
     * Sube una imagen y devuelve la URL pública o null si falla.
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val inputStream: InputStream = resolver.openInputStream(imageUri)
                    ?: return@withContext null

                // Convertir a Bitmap para poder comprimirla
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                val compressedStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, compressedStream)
                val bytes = compressedStream.toByteArray()

                val client = OkHttpClient()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", API_KEY)
                    .addFormDataPart(
                        "image",
                        "profile.jpg",
                        bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val bodyString = response.body?.string()

                if (!response.isSuccessful || bodyString == null) {
                    Log.e(TAG, "HTTP error: ${response.code}")
                    return@withContext null
                }

                val json = JSONObject(bodyString)

                if (json.optBoolean("success")) {
                    val url = json.getJSONObject("data").getString("url")
                    return@withContext url
                }

                Log.e(TAG, "Error del servidor ImgBB ${json.optString("error")}")
                return@withContext null

            } catch (e: Exception) {
                Log.e(TAG, "Error subiendo imagen: ${e.message}")
                return@withContext null
            }
        }
}
