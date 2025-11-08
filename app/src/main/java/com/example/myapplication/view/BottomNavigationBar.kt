package com.example.myapplication.view


import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.R

data class BottomNavItem(val route: String, val label: String, val icon: Int)
@Composable
fun BottomNavigationBar(navController: NavHostController) {
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
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true // evita duplicados
                        }
                    }
                },
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        tint = if (currentRoute == item.route)
                            colors.primary
                        else
                            colors.onSurface.copy(alpha = 0.7f)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (currentRoute == item.route)
                            colors.primary
                        else
                            colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            )
        }
    }
}
