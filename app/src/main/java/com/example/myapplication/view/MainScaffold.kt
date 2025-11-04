package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun MainScaffold(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    content: @Composable (PaddingValues) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background, //color de fondo dinámico
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { BottomNavigationBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* acción del botón + */ },
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        // Aplica el fondo por seguridad (por si la pantalla no lo tiene)
        Surface(
            modifier = Modifier.background(colors.background)
        ) {
            content(padding)
        }
    }
}