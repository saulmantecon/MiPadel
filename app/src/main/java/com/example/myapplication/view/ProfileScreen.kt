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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val usuario by viewModel.usuario.collectAsState() // Observa cambios en tiempo real
    var editMode by remember { mutableStateOf(false) }
    var hasEdited by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var username by remember { mutableStateOf(usuario?.username ?: "") }


    MainScaffold(
        navController = navController,
        isEditing = editMode,
        onEditClick = {
            if (editMode) hasEdited = true
            editMode = !editMode
        }) { padding, snackbarHostState ->

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Cuando editMode se vuelve false (acabas de guardar), se actualiza el usuario
            LaunchedEffect(editMode) {
                if (!editMode && hasEdited) {
                    usuario?.let {
                        val actualizado = it.copy(
                            nombre = nombre.trim(),
                            username = username.trim()
                        )
                        val message = viewModel.updateUsuario(actualizado)
                        snackbarHostState.showSnackbar(message)
                    }
                    hasEdited = false // reseteamos para siguientes usos
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
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                enabled = editMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )
        }
    }
}
