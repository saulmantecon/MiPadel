package com.example.myapplication.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.model.AuthState
import com.example.myapplication.viewmodel.LoginViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    // Campos del formulario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val colors = MaterialTheme.colorScheme

    // ViewModels
    val loginViewModel: LoginViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    // KeepLoggedIn desde DataStore
    val keepLoggedIn by settingsViewModel.keepLoggedIn.collectAsState(initial = false)

    // Estado de autenticación (idle / loading / success / error)
    val authState by loginViewModel.authState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Reacciona a cambios de estado (mostrar snackbar o navegar)
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    (authState as AuthState.Error).message
                )
            }
            is AuthState.Success -> {
                onLoginClick()
                snackbarHostState.showSnackbar(
                    (authState as AuthState.Success).message
                )
            }
            else -> Unit
        }
    }

    // Pantalla completa
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp)
    ) {
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // CONTENIDO PRINCIPAL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logomipadelsinfondo),
                contentDescription = "Logo MiPádel",
                modifier = Modifier
                    .size(320.dp)
                    .clip(CircleShape)
            )

            Text(
                "Inicio de sesión",
                color = colors.onBackground,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Checkbox "Mantener sesión iniciada"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = keepLoggedIn,
                    onCheckedChange = { checked ->
                        settingsViewModel.saveKeepLoggedIn(checked)
                    }
                )
                Text("Mantener sesión iniciada", color = colors.onBackground)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN DE LOGIN
            Button(
                onClick = {
                    loginViewModel.loginUser(
                        email = email.trim(),
                        password = password.trim(),
                        keepLogged = keepLoggedIn,
                        settings = settingsViewModel
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Iniciar sesión", color = colors.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = colors.onBackground,
                modifier = Modifier.clickable(onClick = onRegisterClick)
            )
        }

        // OVERLAY DE CARGA ENCIMA DE LA PANTALLA
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

