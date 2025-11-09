package com.example.myapplication.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isEditing: Boolean = false,
    onEditClick: () -> Unit = {},
    navController: NavHostController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues, SnackbarHostState) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Obtenemos la ruta actual del NavController
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Seleccionamos la pantalla actual según la ruta
    val currentScreen = when (currentRoute) {
        Screen.Home.route -> Screen.Home
        Screen.Community.route -> Screen.Community
        Screen.Profile.route -> Screen.Profile
        else -> Screen.Home
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },

        // TopBar configurada automáticamente según la pantalla actual
        topBar = {
            AppTopBar(
                title = currentScreen.title,
                showMenu = currentScreen.showMenu,
                showBack = currentScreen.showBack,
                showAdd = currentScreen.showAdd,
                showSearch = currentScreen.showSearch,
                showEdit = currentScreen.showEdit,
                onMenuClick = { /* abrir menú lateral */ },
                onAddClick = { /* abrir modal nuevo amigo */ },
                onSearchClick = { /* buscar usuario */ },
                onEditClick = onEditClick,
                isEditing = isEditing,
                )
        },

        bottomBar = {
            BottomNavigationBar(navController)
        },

        floatingActionButton = {
            if (currentScreen.showFab) {
                FloatingActionButton(
                    onClick = { /* acción del FAB */ },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    ) { padding ->
        content(padding, snackbarHostState)
    }
}
