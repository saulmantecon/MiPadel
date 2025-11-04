package com.example.myapplication.view


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R

data class BottomNavItem(val route: String, val label: String, val icon: Int)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("home", "Home", R.drawable.ic_home_padel),
        BottomNavItem("community", "Community", R.drawable.ic_group),
        BottomNavItem("profile", "Profile", R.drawable.ic_profile)
    )

    val colors = MaterialTheme.colorScheme
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = colors.surface,
        contentColor = colors.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        tint = if (currentRoute == item.route) colors.primary else colors.onSurface
                    )
                },
                label = { Text(item.label, color = colors.onSurface) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = colors.primary.copy(alpha = 0.15f),
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    unselectedIconColor = colors.onSurface.copy(alpha = 0.7f),
                    unselectedTextColor = colors.onSurface.copy(alpha = 0.7f)
                )
            )
        }
    }
}