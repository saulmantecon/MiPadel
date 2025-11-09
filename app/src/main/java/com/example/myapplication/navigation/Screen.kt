package com.example.myapplication.navigation

sealed class Screen(
    val route: String,
    val title: String,
    val showMenu: Boolean = false,
    val showBack: Boolean = false,
    val showAdd: Boolean = false,
    val showSearch: Boolean = false,
    val showFab: Boolean = false,
    val showEdit: Boolean = false
) {
    object Home : Screen(
        route = "home",
        title = "Inicio",
        showMenu = true,
        showFab = true
    )

    object Community : Screen(
        route = "community",
        title = "Lista de amigos",
        showMenu = true,
        showAdd = true,
        showSearch = true
    )

    object Profile : Screen(
        route = "profile",
        title = "Perfil",
        showMenu = true,
        showEdit = true
    )
}
