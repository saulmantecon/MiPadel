package com.example.myapplication.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


object FirebaseFirestoreManager {
    val db: FirebaseFirestore by lazy {Firebase.firestore}
}
