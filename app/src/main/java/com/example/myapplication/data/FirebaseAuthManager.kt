package com.example.myapplication.data

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun currentUser() = auth.currentUser
    fun signOut() = auth.signOut()
}
