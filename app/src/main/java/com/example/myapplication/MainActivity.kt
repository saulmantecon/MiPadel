package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.UserPreferencesDataStore
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.navigation.NavigationWrapper
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(dynamicColor = false, darkTheme = true) {

                // TU navController de siempre
                val navController: NavHostController = rememberNavController()

                // DataStore (usando el contexto de la Activity)
                val prefs = UserPreferencesDataStore(this)

                // Auto-login al arrancar
                LaunchedEffect(Unit) {

                    autoLoginFlow(prefs, navController)
                }

                // Tu navegación de siempre, empezando en "login"
                NavigationWrapper(
                    navHostController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Comprueba DataStore:
 * - Si keepLoggedIn es false → ir a login.
 * - Si true pero no hay uid → ir a login.
 * - Si true y hay uid → carga usuario desde Firestore y navega a profile.
 */
private suspend fun autoLoginFlow(
    prefs: UserPreferencesDataStore,
    navController: NavHostController
) {
    // 1. ¿El usuario pidió mantener sesión?
    val keepLoggedIn = prefs.keepLoggedIn.first()

    if (!keepLoggedIn) {
        // Aseguramos que el start sea login
        navController.navigate("login") {
            popUpTo(0)
        }
        return
    }

    // 2. ¿Hay uid guardado?
    val savedUid = prefs.savedUserUid.first()

    if (savedUid == null) {
        navController.navigate("login") {
            popUpTo(0)
        }
        return
    }

    // 3. Intentamos cargar el usuario desde Firestore
    val result = UsuarioRepository.obtenerUsuario(savedUid)

    result.fold(
        onSuccess = { usuario ->
            // Guardamos el usuario global (para Profile, Estadísticas, etc.)
            CurrentUserManager.setUsuario(usuario)

            // Navegamos al perfil y vaciamos backstack
            navController.navigate("profile") {
                popUpTo(0)
            }
        },
        onFailure = {
            // Algo fue mal, volvemos a login
            navController.navigate("login") {
                popUpTo(0)
            }
        }
    )
}
