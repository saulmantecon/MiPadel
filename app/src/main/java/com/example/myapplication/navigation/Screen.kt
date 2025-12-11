package com.example.myapplication.navigation

/**
 * Representa cada pantalla de la app junto a:
 * - su ruta de navegación
 * - su título
 * - qué elementos del TopBar se muestran
 * - si muestra FAB
 *
 * Esta clase permite centralizar toda la configuración visual
 * de la navegación, haciendo que MainScaffold sea dinámico.
 */
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
        showMenu = true
    )

    object Profile : Screen(
        route = "profile",
        title = "Perfil",
        showMenu = true,
        showEdit = true
    )

    object Estadisticas : Screen(
        route = "estadisticas",
        title = "Estadísticas",
        showBack = true
    )
}
