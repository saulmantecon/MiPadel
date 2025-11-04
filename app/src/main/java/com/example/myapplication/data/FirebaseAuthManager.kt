package com.example.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

//Singleton garantizando una instancia de FirebaseAuth en toda la app.
object FirebaseAuthManager {
    // Instancia de FirebaseAuth
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Devuelve el usuario actual o null
    fun currentUser(): FirebaseUser? = auth.currentUser

    // Devuelve true si hay sesión iniciada
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Devuelve el correo del usuario actual (si existe)
    fun getUserEmail(): String? = auth.currentUser?.email

    // Cierra la sesión
    fun signOut() = auth.signOut()
}
