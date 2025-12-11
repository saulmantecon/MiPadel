package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.data.UserPreferencesDataStore
import com.example.myapplication.data.repository.UsuarioRepository
import com.example.myapplication.model.MainState
import com.example.myapplication.navigation.NavigationWrapper
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Estado global para controlar si la app está lista (splash) + ruta inicial
    private val mainState = mutableStateOf(MainState())

    override fun onCreate(savedInstanceState: Bundle?) {

        // 1. SplashScreen nativo (debe declararse antes de super.onCreate)
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Mantener el splash visible hasta que mainState.ready = true
        splash.setKeepOnScreenCondition {
            !mainState.value.ready
        }

        // 3. Ejecutar autologin ANTES de componer la UI
        lifecycleScope.launch {
            autoLoginBeforeCompose()
        }

        // 4. Composición principal
        setContent {
            AppContent()
        }
    }

    /**
     * Autologin inicial. Lee el DataStore directamente para:
     * - Saber si keepLoggedIn está activado
     * - Recuperar el UID guardado
     *
     * Esto evita instanciar ViewModels antes de Compose.
     */
    private suspend fun autoLoginBeforeCompose() {

        val prefs = UserPreferencesDataStore(this)

        val keepLogged = prefs.keepLoggedIn.first()
        val uid = prefs.savedUserUid.first()

        // Si no se debe mantener login -> ir a Login
        if (!keepLogged || uid == null) {
            mainState.value = MainState(
                ready = true,
                startDestination = "login"
            )
            return
        }

        // Se intenta recuperar el usuario de Firestore
        val result = UsuarioRepository.obtenerUsuario(uid)

        result.fold(
            onSuccess = { user ->
                // Guardamos usuario en memoria global
                CurrentUserManager.setUsuario(user)

                mainState.value = MainState(
                    ready = true,
                    startDestination = "home"
                )
            },
            onFailure = {
                // Si algo falla -> login
                mainState.value = MainState(
                    ready = true,
                    startDestination = "login"
                )
            }
        )
    }

    /**
     * Contenido principal de la app:
     * - Aplica el tema (oscuro, claro o system)
     * - Prepara el NavController
     * - Llama al NavigationWrapper
     */
    @Composable
    private fun AppContent() {

        val state = mainState.value

        // ViewModel para ajustes globales (tema)
        val settingsViewModel: SettingsViewModel = viewModel()

        // Observar el modo de tema desde DataStore
        val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")

        // Determinar darkTheme según preferencia
        val darkTheme = when (themeMode) {
            "light" -> false
            "dark" -> true
            else -> isSystemInDarkTheme()
        }

        // Mientras se carga el autologin -> pantalla sólida sin parpadeos
        if (!state.ready) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F3D1E))
            )
            return
        }

        // Tema principal
        MyApplicationTheme(
            darkTheme = darkTheme,
            dynamicColor = false
        ) {
            val navController = rememberNavController()

            // Contenedor principal de navegación
            NavigationWrapper(
                navHostController = navController,
                startDestination = state.startDestination,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

