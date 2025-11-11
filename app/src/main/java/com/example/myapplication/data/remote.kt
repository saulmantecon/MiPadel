package com.example.myapplication.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InputStream

object ImageUploader {

    private const val API_KEY = "0ba151ae2e01275107fd5c2253a1a666"
    private const val TAG = "ImageUploader"

    suspend fun uploadImage(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes() ?: return@withContext null

            val client = OkHttpClient()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", API_KEY)
                .addFormDataPart(
                    "image",
                    "upload.jpg",
                    bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size)
                )
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string()

            if (!response.isSuccessful || bodyString == null) {
                Log.e(TAG, "HTTP error: ${response.code} - ${response.message}")
                return@withContext null
            }

            // 🔍 Parseamos el JSON de respuesta
            val json = JSONObject(bodyString)

            if (json.optBoolean("success")) {
                val data = json.getJSONObject("data")
                val imageUrl = data.getString("url")
                Log.i(TAG, "Imagen subida correctamente: $imageUrl")
                return@withContext imageUrl
            } else {
                val errorMessage = json.optString("error", "Error desconocido")
                Log.e(TAG, "Error del servidor ImgBB: $errorMessage")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Excepción al subir imagen: ${e.message}", e)
            return@withContext null
        }
    }
}
