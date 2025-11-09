package com.example.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

//Singleton garantizando una instancia de FirebaseAuth en toda la app.
object FirebaseAuthManager {
    // Instancia de FirebaseAuth
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}
