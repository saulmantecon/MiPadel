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
import com.example.myapplication.navigation.NavigationWrapper
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Estado para saber si app está lista + ruta inicial
    private val mainState = mutableStateOf(MainState())

    override fun onCreate(savedInstanceState: Bundle?) {

        // 1. SplashScreen antes de super.onCreate()
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 2. Mantener Splash hasta que mainState.ready = true
        splash.setKeepOnScreenCondition {
            !mainState.value.ready
        }

        // 3. Autologin ANTES de Compose
        lifecycleScope.launch {
            autoLoginBeforeCompose()
        }

        // 4. UI Compose
        setContent {
            AppContent()
        }
    }

    /**
     * AUTLOGIN sin duplicar SettingsViewModel.
     * Se leen los valores DIRECTAMENTE del DataStore.
     */
    private suspend fun autoLoginBeforeCompose() {

        val prefs = UserPreferencesDataStore(this)

        val keepLogged = prefs.keepLoggedIn.first()
        val uid = prefs.savedUserUid.first()

        if (!keepLogged || uid == null) {
            mainState.value = MainState(
                ready = true,
                startDestination = "login"
            )
            return
        }

        val result = UsuarioRepository.obtenerUsuario(uid)

        result.fold(
            onSuccess = {
                CurrentUserManager.setUsuario(it)
                mainState.value = MainState(
                    ready = true,
                    startDestination = "profile"
                )
            },
            onFailure = {
                mainState.value = MainState(
                    ready = true,
                    startDestination = "login"
                )
            }
        )
    }

    /**
     * Tema dinámico + NavHost
     */
    @Composable
    private fun AppContent() {
        val state = mainState.value

        // Este ES el único SettingsViewModel global
        val settingsViewModel: SettingsViewModel = viewModel()

        // Observar themeMode desde DataStore
        val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")

        val darkTheme = when (themeMode) {
            "light" -> false
            "dark" -> true
            else -> isSystemInDarkTheme()
        }

        // Mientras no esté listo → mostrar pantalla sólida (sin flash)
        if (!state.ready) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F3D1E))
            )
            return
        }

        MyApplicationTheme(
            darkTheme = darkTheme,
            dynamicColor = false
        ) {
            val navController = rememberNavController()

            NavigationWrapper(
                navHostController = navController,
                startDestination = state.startDestination,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Estado simple para splash + ruta inicial
 */
data class MainState(
    val ready: Boolean = false,
    val startDestination: String = "login"
)
