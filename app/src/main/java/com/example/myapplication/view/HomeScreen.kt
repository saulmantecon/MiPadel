package com.example.myapplication.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.CurrentUserManager
import com.example.myapplication.model.Partido
import com.example.myapplication.model.Usuario
import com.example.myapplication.viewmodel.CrearPartidoViewModel
import com.example.myapplication.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    crearPartidoViewModel: CrearPartidoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val partidos by homeViewModel.partidos.collectAsState()
    val mensaje by homeViewModel.mensaje.collectAsState()
    val usuariosMapa by homeViewModel.usuarios.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            scope.launch { snackbarHostState.showSnackbar(mensaje!!) }
            homeViewModel.limpiarMensaje()
        }
    }

    MainScaffold(
        navController = navController,
        snackbarHostState = snackbarHostState,
        onFabClick = { showSheet = true }
    ) { padding, _ ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (partidos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay partidos todavía")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(partidos) { partido ->

                        // Pedimos info de usuarios de cada posición
                        partido.posiciones.filter { it.isNotBlank() }.forEach { uid ->
                            LaunchedEffect(uid) {
                                homeViewModel.solicitarUsuario(uid)
                            }
                        }

                        PartidoCard(
                            partido = partido,
                            currentUid = homeViewModel.currentUid,
                            usuariosMapa = usuariosMapa,
                            onClickPosicion = { index ->
                                val uidEnPos = partido.posiciones.getOrNull(index).orEmpty()

                                when {
                                    uidEnPos.isBlank() -> {
                                        homeViewModel.ocuparPosicion(partido, index)
                                    }
                                    uidEnPos == homeViewModel.currentUid -> {
                                        homeViewModel.salirDePartido(partido.id)
                                    }
                                    else -> {
                                        homeViewModel.mostrarMensaje("Esa posición ya está ocupada")
                                    }
                                }
                            },
                            onBorrarPartido = {
                                homeViewModel.borrarPartido(partido.id)
                            }
                        )
                    }
                }
            }

            if (showSheet) {
                CrearPartidoBottomSheet(
                    viewModel = crearPartidoViewModel,
                    onCerrar = { showSheet = false },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun PartidoCard(
    partido: Partido,
    currentUid: String,
    usuariosMapa: Map<String, Usuario>,
    onClickPosicion: (Int) -> Unit,
    onBorrarPartido: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        partido.ubicacion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    partido.fecha?.toDate()?.let {
                        Text(
                            it.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                if (partido.creadorId == currentUid) {
                    IconButton(onClick = onBorrarPartido) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = colors.error
                        )
                    }
                }
            }

            // Estado sencillo
            val completo = partido.posiciones.none { it.isBlank() }
            if (completo) {
                Text(
                    "PARTIDO COMPLETO",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary
                )
            }

            // Zona jugadores P1/P2 vs P3/P4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PosicionBox(
                        index = 0,
                        uid = partido.posiciones.getOrNull(0),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(0)],
                        onClick = { onClickPosicion(0) }
                    )
                    PosicionBox(
                        index = 1,
                        uid = partido.posiciones.getOrNull(1),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(1)],
                        onClick = { onClickPosicion(1) }
                    )
                }

                Text("VS", style = MaterialTheme.typography.titleMedium)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PosicionBox(
                        index = 2,
                        uid = partido.posiciones.getOrNull(2),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(2)],
                        onClick = { onClickPosicion(2) }
                    )
                    PosicionBox(
                        index = 3,
                        uid = partido.posiciones.getOrNull(3),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(3)],
                        onClick = { onClickPosicion(3) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PosicionBox(
    index: Int,
    uid: String?,
    usuario: Usuario?,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (uid.isNullOrBlank()) {
            Text(
                "Vacío",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (usuario?.fotoPerfilUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = usuario.fotoPerfilUrl,
                        contentDescription = "Foto perfil",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Text(
                    text = usuario?.username ?: "Jugador",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}