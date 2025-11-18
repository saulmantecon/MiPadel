package com.example.myapplication.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.model.Amistad
import com.example.myapplication.model.Usuario
import com.example.myapplication.viewmodel.CommunityViewModel
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(
    navController: NavHostController,
    viewModel: CommunityViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()


    // Estados observables
    val amigos by viewModel.amigos.collectAsState()
    val solicitudes by viewModel.solicitudes.collectAsState()
    val resultadosBusqueda by viewModel.busqueda.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSolicitudes by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadAmigos()
        viewModel.loadSolicitudes()
    }

    MainScaffold(navController = navController) { padding, snackbar ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // 1. SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.buscarUsuarios(it)
                },
                label = { Text("Buscar usuarios…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )


            // 2. RESULTADOS DE BÚSQUEDA
            if (resultadosBusqueda.isNotEmpty()) {
                Text(
                    "Usuarios encontrados",
                    style = MaterialTheme.typography.titleMedium
                )

                resultadosBusqueda.forEach { usuario ->
                    UsuarioBusquedaItem(
                        usuario = usuario,
                        onEnviarSolicitud = {
                            viewModel.enviarSolicitud(usuario.uid) { msg ->
                                scope.launch {
                                    snackbar.showSnackbar(msg)
                                }
                            }
                        }
                    )
                }
            }


            // 3. SOLICITUDES RECIBIDAS (DESPLEGABLE)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSolicitudes = !showSolicitudes }
            ) {
                Text(
                    "Solicitudes de amistad (${solicitudes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (showSolicitudes) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (showSolicitudes && solicitudes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    solicitudes.forEach { (amistad, usuario) ->
                        SolicitudItem(
                            amistad = amistad,
                            usuario = usuario,
                            onAceptar = { viewModel.aceptarSolicitud(amistad) },
                            onRechazar = { viewModel.rechazarSolicitud(amistad) }
                        )
                    }
                }
            }


            // 4. LISTA DE AMIGOS
            Text(
                "Tus amigos (${amigos.size})",
                style = MaterialTheme.typography.titleMedium
            )

            amigos.forEach { amigo ->
                AmigoItem(
                    usuario = amigo,
                    onEliminar = {
                        viewModel.eliminarAmigo(amigo.uid) { msg ->
                            scope.launch {
                                snackbar.showSnackbar(msg)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UsuarioBusquedaItem(
    usuario: Usuario,
    onEnviarSolicitud: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var enviado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // FOTO
            AsyncImage(
                model = usuario.fotoPerfilUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.username, style = MaterialTheme.typography.titleMedium)
                Text(usuario.nombre, style = MaterialTheme.typography.bodyMedium)
            }

            Button(onClick = {
                onEnviarSolicitud()
                enviado = true
            }) {
                Text(if (enviado) "Enviado" else "Enviar")
            }
        }
    }
}

@Composable
fun SolicitudItem(
    amistad: Amistad,
    usuario: Usuario,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    usuario.username,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Te ha enviado una solicitud")
            }

            IconButton(onClick = onAceptar) {
                Icon(Icons.Default.Check, contentDescription = "Aceptar")
            }
            IconButton(onClick = onRechazar) {
                Icon(Icons.Default.Clear, contentDescription = "Rechazar")
            }
        }
    }
}

@Composable
fun AmigoItem(
    usuario: Usuario,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = usuario.fotoPerfilUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.username, style = MaterialTheme.typography.titleMedium)
                Text("Nivel: ${usuario.nivel}")
            }

            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar amigo")
            }
        }
    }
}

