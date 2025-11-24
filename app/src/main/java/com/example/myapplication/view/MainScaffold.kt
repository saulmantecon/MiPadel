package com.example.myapplication.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.navigation.Screen
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    isEditing: Boolean = false,
    onEditClick: () -> Unit = {},
    onFabClick: () -> Unit = {},
    navController: NavHostController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    settingsViewModel: SettingsViewModel = viewModel(),
    content: @Composable (PaddingValues, SnackbarHostState) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Obtenemos la ruta actual del NavController
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Seleccionamos la pantalla actual según la ruta
    val currentScreen = when (currentRoute) {
        Screen.Home.route -> Screen.Home
        Screen.Community.route -> Screen.Community
        Screen.Profile.route -> Screen.Profile
        Screen.Estadisticas.route -> Screen.Estadisticas
        else -> Screen.Home
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.35f),
        gesturesEnabled = true,
        drawerContent = {
            DrawerContent(
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    settingsViewModel.logout()
                    navController.navigate("login") { popUpTo(0) }
                },
                settingsViewModel = settingsViewModel
            )
        }

    ) {

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = colors.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },

            topBar = {
                AppTopBar(
                    title = currentScreen.title,
                    showMenu = currentScreen.showMenu,
                    showBack = currentScreen.showBack,
                    showAdd = currentScreen.showAdd,
                    showSearch = currentScreen.showSearch,
                    showEdit = currentScreen.showEdit,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onAddClick = { /* lo que quieras */ },
                    onSearchClick = { /* lo que quieras */ },
                    onEditClick = onEditClick,
                    isEditing = isEditing,
                    onBackClick = { navController.popBackStack() }
                )
            },

            bottomBar = {
                BottomNavigationBar(navController)
            },

            floatingActionButton = {
                if (currentScreen.showFab) {
                    FloatingActionButton(
                        onClick = onFabClick,
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
}
