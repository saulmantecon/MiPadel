package com.example.myapplication.model

/**
 * Modelo simple para manejar:
 * - Estado del SplashScreen
 * - Ruta inicial de la app
 */
data class MainState(
    val ready: Boolean = false,
    val startDestination: String = "login"
)