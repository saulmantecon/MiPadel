package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.view.*

/**
 * Contenedor principal de navegación.
 *
 * Se encarga de:
 * - Crear el NavHost
 * - Registrar todas las pantallas (rutas)
 * - Gestionar navegación básica entre pantallas
 *
 * NavigationWrapper se usa desde MainActivity y define
 * toda la estructura de navegación de la app.
 */
@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable("login") {
            LoginScreen(
                onLoginClick = {
                    // Tras login -> ir a Home
                    navHostController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navHostController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterClick = {
                    // Tras registro -> ir a Home
                    navHostController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navHostController.navigate("login")
                }
            )
        }

        composable("home") {
            HomeScreen(navHostController)
        }

        composable("community") {
            CommunityScreen(navHostController)
        }

        composable("profile") {
            ProfileScreen(
                navController = navHostController,
                onVerEstadisticas = {
                    navHostController.navigate("estadisticas")
                }
            )
        }

        composable("estadisticas") {
            EstadisticasScreen(navHostController)
        }
    }
}
