package com.example.myapplication.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.viewmodel.ProfileViewModel


@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val usuario by viewModel.usuario.collectAsState() // Observa cambios en tiempo real
    var editMode by remember { mutableStateOf(false) }

    MainScaffold(navController = navController) { padding ->

        if (usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@MainScaffold
        }

        var nombre by remember(usuario) { mutableStateOf(usuario!!.nombre) }
        var nivel by remember(usuario) { mutableStateOf(usuario!!.nivel.toString()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // CABECERA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil de usuario",
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = {
                    if (editMode) {
                        val actualizado = usuario!!.copy(
                            nombre = nombre,
                            nivel = nivel.toIntOrNull() ?: usuario!!.nivel
                        )
                        viewModel.updateUsuario(actualizado)
                    }
                    editMode = !editMode
                }) {
                    Icon(
                        imageVector = if (editMode) Icons.Default.Done else Icons.Default.Edit,
                        contentDescription = if (editMode) "Guardar" else "Editar"
                    )
                }
            }

            // CAMPOS
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre y apellidos") },
                enabled = editMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            OutlinedTextField(
                value = usuario!!.email,
                onValueChange = {},
                label = { Text("Correo electrónico") },
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            OutlinedTextField(
                value = nivel,
                onValueChange = { nivel = it },
                label = { Text("Nivel") },
                enabled = editMode,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )
        }
    }
}
