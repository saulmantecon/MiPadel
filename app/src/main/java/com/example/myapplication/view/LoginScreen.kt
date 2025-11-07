package com.example.myapplication.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.model.AuthState
import com.example.myapplication.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepLoggedIn by remember { mutableStateOf(true) }
    val viewModel: LoginViewModel = viewModel()
    /*
    val authState by viewModel.authState.collectAsState()
    1. Suscribe la pantalla al flujo authState del ViewModel.
    2. Convierte el StateFlow en un State de Compose (para que se pueda usar en when, Text(), etc.).
    3.Redibuja automáticamente la UI cada vez que el estado cambia.
     */
    val authState by viewModel.authState.collectAsState()

    /*
     remember guarda el objeto entre recomposiciones.
     by desempaqueta automáticamente el valor de un State observable.
     */
    val snackbarHostState = remember { SnackbarHostState() }


    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background) // Se adapta al modo claro/oscuro
            .padding(24.dp)
    ) {
        //Se ejecuta SOLO cuando cambia la key authState
        LaunchedEffect(authState) {
            when (authState) {
                is AuthState.Error -> {
                    snackbarHostState.showSnackbar(
                        message = (authState as AuthState.Error).message,
                        withDismissAction = true
                    )
                }

                is AuthState.Success -> {
                    onLoginClick()
                    //Muestra el snackbar
                    snackbarHostState.showSnackbar(
                        message = (authState as AuthState.Success).message,
                        withDismissAction = true
                    )
                    //Navega a Home
                }

                else -> Unit
            }
        }
        //Overlay de carga
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .zIndex(2f), //lo pone encima de todo
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        // SnackbarHost — donde aparecerán los mensajes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter) // posición inferior
                .padding(bottom = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Logo
            Image(
                painter = painterResource(id = R.drawable.logomipadel),
                contentDescription = "Logo MiPádel",
                modifier = Modifier
                    .size(320.dp)
                    .padding(bottom = 16.dp)
                    .clip(CircleShape)
            )

            Text(
                text = "Inicio de sesión",
                color = colors.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            //textfieldUsuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario", color = colors.onSurface.copy(alpha = 0.7f)) },
                singleLine = true,
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = colors.onSurface.copy(alpha = 0.7f)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = keepLoggedIn,
                    onCheckedChange = { keepLoggedIn = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primary,
                        uncheckedColor = colors.onBackground
                    )
                )
                Text(
                    text = "Mantener sesión iniciada",
                    color = colors.onBackground,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar Sesión
            Button(
                onClick = { viewModel.loginUser(username.trim(), password.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto final
            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = colors.onBackground,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }
}
