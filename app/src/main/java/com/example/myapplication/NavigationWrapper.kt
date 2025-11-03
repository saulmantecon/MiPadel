package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.view.HomeScreen
import com.example.myapplication.view.LoginScreen
import com.example.myapplication.view.RegisterScreen

@Composable
fun NavigationWrapper(navHostController: NavHostController, modifier: Modifier) {
    NavHost(navController = navHostController, startDestination = "login", modifier = modifier){
        composable("login"){
            LoginScreen( onLoginClick = {
                navHostController.navigate("home")},
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
            HomeScreen()
        }
        composable("community") {  }

        composable("profile") {  }
    }
}