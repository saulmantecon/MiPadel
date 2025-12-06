package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.model.Partido
import com.example.myapplication.model.Usuario
import com.example.myapplication.viewmodel.CrearPartidoViewModel
import com.example.myapplication.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    crearPartidoViewModel: CrearPartidoViewModel = viewModel()
) {
    val partidos by homeViewModel.partidos.collectAsState()
    val usuariosMapa by homeViewModel.usuarios.collectAsState()

    var showSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    MainScaffold(
        navController = navController,
        sheetVisible = showSheet,
        onFabClick = { showSheet = true }
    ) { padding, snackbarHostState ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val crearMensaje by crearPartidoViewModel.mensaje.collectAsState()

            LaunchedEffect(crearMensaje) {
                crearMensaje?.let { msg ->

                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }

                    if (msg == "Partido creado correctamente") {
                        showSheet = false
                        crearPartidoViewModel.resetForm()
                    }

                    crearPartidoViewModel.limpiarMensaje()
                }
            }

            if (partidos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay partidos todavía")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(partidos) { partido ->

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
                                homeViewModel.ocuparPosicion(partido, index) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }

                                }
                            },
                            onBorrarPartido = {
                                homeViewModel.borrarPartido(partido.id) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                }
                            },
                            onFinalizarPartido = { sets ->
                                homeViewModel.finalizarPartido(partido, sets) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (showSheet) {
                CrearPartidoBottomSheet(
                    viewModel = crearPartidoViewModel,
                    onCerrar = { showSheet = false }
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
    onBorrarPartido: () -> Unit,
    onFinalizarPartido: (List<Pair<String, String>>) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val dateFormat = remember { SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // estado local para el editor de resultado
    var set1Eq1 by remember(partido.id) { mutableStateOf("") }
    var set1Eq2 by remember(partido.id) { mutableStateOf("") }
    var set2Eq1 by remember(partido.id) { mutableStateOf("") }
    var set2Eq2 by remember(partido.id) { mutableStateOf("") }
    var set3Eq1 by remember(partido.id) { mutableStateOf("") }
    var set3Eq2 by remember(partido.id) { mutableStateOf("") }

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

            // CABECERA: ubicación + fecha + botón borrar (creador)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        partido.ubicacion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    partido.fecha?.toDate()?.let {
                        Text(
                            dateFormat.format(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    EstadoChip(estado = partido.estado)

                    if (partido.creadorId == currentUid &&
                        partido.estado != "finalizado"
                    ) {
                        IconButton(onClick = onBorrarPartido) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = colors.error
                            )
                        }
                    }
                }
            }

            // ZONA JUGADORES P1/P2 vs P3/P4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PosicionBox(
                        uid = partido.posiciones.getOrNull(0),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(0)],
                        onClick = { onClickPosicion(0) }
                    )
                    PosicionBox(
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
                        uid = partido.posiciones.getOrNull(2),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(2)],
                        onClick = { onClickPosicion(2) }
                    )
                    PosicionBox(
                        uid = partido.posiciones.getOrNull(3),
                        usuario = usuariosMapa[partido.posiciones.getOrNull(3)],
                        onClick = { onClickPosicion(3) }
                    )
                }
            }

            // SI EL PARTIDO ESTÁ FINALIZADO: mostramos resultado
            if (partido.estado == "finalizado" && partido.sets.isNotEmpty()) {
                val textoSets = partido.sets.joinToString("  ·  ") {
                    "${it.juegosEquipo1}-${it.juegosEquipo2}"
                }
                Text(
                    text = "Resultado: $textoSets",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // SI ESTÁ JUGANDO Y ES EL CREADOR: mostramos editor de resultado
            if (partido.estado == "jugando" && partido.creadorId == currentUid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Introduce el resultado",
                    style = MaterialTheme.typography.bodyMedium
                )

                ResultadoRow(
                    label = "Set 1",
                    valueEq1 = set1Eq1,
                    onValueEq1Change = { set1Eq1 = it },
                    valueEq2 = set1Eq2,
                    onValueEq2Change = { set1Eq2 = it }
                )
                ResultadoRow(
                    label = "Set 2",
                    valueEq1 = set2Eq1,
                    onValueEq1Change = { set2Eq1 = it },
                    valueEq2 = set2Eq2,
                    onValueEq2Change = { set2Eq2 = it }
                )
                ResultadoRow(
                    label = "Set 3 (opcional)",
                    valueEq1 = set3Eq1,
                    onValueEq1Change = { set3Eq1 = it },
                    valueEq2 = set3Eq2,
                    onValueEq2Change = { set3Eq2 = it }
                )

                Button(
                    onClick = {
                        onFinalizarPartido(
                            listOf(
                                set1Eq1 to set1Eq2,
                                set2Eq1 to set2Eq2,
                                set3Eq1 to set3Eq2
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Finalizar partido")
                }
            }
        }
    }
}

@Composable
private fun EstadoChip(estado: String) {
    val colors = MaterialTheme.colorScheme

    val (texto, bg, fg) = when (estado) {
        "pendiente" -> Triple("Pendiente", colors.surfaceVariant, colors.onSurfaceVariant)
        "listo" -> Triple("Completo", colors.primary.copy(alpha = 0.15f), colors.primary)
        "jugando" -> Triple("¡Jugando!", Color(0xFFFFF59D), Color(0xFFF57F17))
        "finalizado" -> Triple("Finalizado", colors.tertiary.copy(alpha = 0.2f), colors.tertiary)
        else -> Triple(estado, colors.surfaceVariant, colors.onSurfaceVariant)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}

@Composable
private fun PosicionBox(
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

@Composable
private fun ResultadoRow(
    label: String,
    valueEq1: String,
    onValueEq1Change: (String) -> Unit,
    valueEq2: String,
    onValueEq2Change: (String) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.width(90.dp),
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedTextField(
            value = valueEq1,
            onValueChange = { if (it.length <= 2) onValueEq1Change(it) },
            modifier = Modifier.weight(1f).height(48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },

        )

        Text("-", modifier = Modifier.padding(horizontal = 4.dp))

        OutlinedTextField(
            value = valueEq2,
            onValueChange = { if (it.length <= 2) onValueEq2Change(it) },
            modifier = Modifier.weight(1f).height(48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
        )
    }
}