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
    val mensajeHome by homeViewModel.mensaje.collectAsState()

    // Control del BottomSheet
    var showSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mostrar snackbar para mensajes de unirse/salir/borrar
    LaunchedEffect(mensajeHome) {
        if (mensajeHome != null) {
            scope.launch {
                snackbarHostState.showSnackbar(mensajeHome!!)
            }
            homeViewModel.limpiarMensaje()
        }
    }

    // Contenido con tu MainScaffold
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

            // Lista de partidos
            if (partidos.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
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

                        val jugadores = homeViewModel.obtenerJugadores(partido)
                        val currentUid = homeViewModel.currentUid

                        PartidoCard(
                            partido = partido,
                            jugadores = jugadores,
                            currentUid = currentUid,
                            onOcuparHueco = { slot ->
                                homeViewModel.ocuparHueco(partido.id, slot)
                            },
                            onSalirHueco = { slot ->
                                homeViewModel.salirDeHueco(partido.id, slot)
                            },
                            onBorrarPartido = {
                                homeViewModel.borrarPartido(partido.id)
                            }
                        )
                    }
                }
            }

            // Bottom Sheet para crear partido
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
    jugadores: List<Usuario?>,
    currentUid: String,
    onOcuparHueco: (Int) -> Unit,
    onSalirHueco: (Int) -> Unit,
    onBorrarPartido: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ------------------------
            //  CABECERA: ubicación + fecha + borrar si creador
            // ------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        partido.ubicacion,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
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
                            Icons.Default.Delete,
                            contentDescription = "Borrar partido",
                            tint = colors.error
                        )
                    }
                }
            }

            // Estado del partido
            if (partido.jugadores.size >= 4) {
                Text(
                    "PARTIDO COMPLETO",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ------------------------
            //  ZONA JUGADORES: 2 VS 2
            // ------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HuecoJugador(
                        slot = 0,
                        usuario = jugadores.getOrNull(0),
                        currentUid = currentUid,
                        onOcuparHueco = onOcuparHueco,
                        onSalirHueco = onSalirHueco
                    )
                    HuecoJugador(
                        slot = 1,
                        usuario = jugadores.getOrNull(1),
                        currentUid = currentUid,
                        onOcuparHueco = onOcuparHueco,
                        onSalirHueco = onSalirHueco
                    )
                }

                // Etiqueta VS
                Text(
                    "VS",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HuecoJugador(
                        slot = 2,
                        usuario = jugadores.getOrNull(2),
                        currentUid = currentUid,
                        onOcuparHueco = onOcuparHueco,
                        onSalirHueco = onSalirHueco
                    )
                    HuecoJugador(
                        slot = 3,
                        usuario = jugadores.getOrNull(3),
                        currentUid = currentUid,
                        onOcuparHueco = onOcuparHueco,
                        onSalirHueco = onSalirHueco
                    )
                }
            }
        }
    }
}



@Composable
private fun HuecoJugador(
    slot: Int,
    usuario: Usuario?,
    currentUid: String,
    onOcuparHueco: (Int) -> Unit,
    onSalirHueco: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(120.dp, 64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .clickable {
                if (usuario == null) {
                    onOcuparHueco(slot)
                } else if (usuario.uid == currentUid) {
                    onSalirHueco(slot)
                }
            }
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // FOTO o icono
            if (usuario?.fotoPerfilUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = usuario.fotoPerfilUrl,
                    contentDescription = "Foto perfil",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.onSurface
                    )
                }
            }

            // Nombre del usuario
            Text(
                text = usuario?.username ?: "Vacío",
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}