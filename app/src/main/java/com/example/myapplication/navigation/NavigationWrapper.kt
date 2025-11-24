package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.view.CommunityScreen
import com.example.myapplication.view.EstadisticasScreen
import com.example.myapplication.view.HomeScreen
import com.example.myapplication.view.LoginScreen
import com.example.myapplication.view.ProfileScreen
import com.example.myapplication.view.RegisterScreen

@Composable
fun NavigationWrapper(navHostController: NavHostController, startDestination: String, modifier: Modifier) {
    NavHost(navController = navHostController, startDestination = startDestination, modifier = modifier){
        composable("login"){
            LoginScreen( onLoginClick = {
                navHostController.navigate("profile")},
                onRegisterClick = {
                    navHostController.navigate("register")
                }
            )
        }
        composable("register"){
            RegisterScreen(
                onRegisterClick = {
                    navHostController.navigate("profile")
                },
                onLoginClick = {
                    navHostController.navigate("login")
                }
            )
        }
        composable("home"){
            HomeScreen(navHostController)
        }
        composable("community") {
            CommunityScreen(navHostController)
        }

        composable("profile") {
            ProfileScreen(navHostController,
                onVerEstadisticas = { navHostController.navigate("estadisticas") })
        }
        composable("estadisticas"){
            EstadisticasScreen(navHostController)
        }
    }
}