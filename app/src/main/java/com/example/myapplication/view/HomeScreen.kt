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
import androidx.compose.ui.text.style.TextOverflow
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
    // Lista de partidos en tiempo real desde el ViewModel
    val partidos by homeViewModel.partidos.collectAsState()

    // Cache de usuarios (uid -> Usuario) para mostrar nombres y fotos
    val usuariosMapa by homeViewModel.usuarios.collectAsState()

    // Controla si el bottom sheet de "Crear partido" está visible
    var showSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    MainScaffold(
        navController = navController,
        sheetVisible = showSheet,
        onFabClick = { showSheet = true }   // Al pulsar el FAB, abrimos el bottom sheet
    ) { padding, snackbarHostState ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Escuchar el mensaje que viene de CrearPartidoViewModel
            val crearMensaje by crearPartidoViewModel.mensaje.collectAsState()

            LaunchedEffect(crearMensaje) {
                crearMensaje?.let { msg ->
                    // Mostrar mensaje
                    scope.launch { snackbarHostState.showSnackbar(msg) }

                    // Si la creación fue correcta -> cerrar sheet y limpiar formulario
                    if (msg == "Partido creado correctamente") {
                        showSheet = false
                        crearPartidoViewModel.resetForm()
                    }

                    // Limpia el mensaje para que no se repita al recomponer
                    crearPartidoViewModel.limpiarMensaje()
                }
            }

            // CONTENIDO PRINCIPAL:
            // Si no hay partidos: texto vacío
            // Si hay partidos: lista de partidos
            if (partidos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay partidos todavía",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(partidos) { partido ->

                        // Cargar los datos de los jugadores de este partido
                        // (solo una vez por partido)
                        LaunchedEffect(partido.posiciones) {
                            partido.posiciones
                                .filter { it.isNotBlank() }
                                .forEach { uid ->
                                    homeViewModel.solicitarUsuario(uid)
                                }
                        }

                        PartidoCard(
                            partido = partido,
                            currentUid = homeViewModel.currentUid,
                            usuariosMapa = usuariosMapa,

                            // Cuando el usuario pulsa una posición, intentamos unirle al partido
                            onClickPosicion = { index ->
                                val uidEnSlot = partido.posiciones[index]

                                if (uidEnSlot == homeViewModel.currentUid) {
                                    // Salir del partido
                                    homeViewModel.salirDePartido(partido) {
                                        scope.launch { snackbarHostState.showSnackbar(it) }
                                    }
                                } else {
                                    // Intentar ocupar posición
                                    homeViewModel.ocuparPosicion(partido, index) {
                                        scope.launch { snackbarHostState.showSnackbar(it) }
                                    }
                                }
                            },

                            // El creador puede borrar su partido
                            onBorrarPartido = {
                                homeViewModel.borrarPartido(partido.id) { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },

                            // El creador puede finalizar un partido en juego
                            onFinalizarPartido = { setsIntroducidos ->
                                homeViewModel.finalizarPartido(partido, setsIntroducidos) { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        )
                    }
                }
            }

            // BOTTOM SHEET: formulario para crear un nuevo partido
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

    // Formato de la fecha
    val dateFormat = remember {
        SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    //Estado local del editor de sets
    // Guardamos 3 sets posibles como lista de Pair<String, String>
    //  - first  -> juegos equipo 1
    //  - second -> juegos equipo 2
    // remember(partido.id) hace que el estado se resetee cuando
    // se muestra una tarjeta de otro partido.
    // ----------------------------------------------------------
    var setsValues by remember(partido.id) {
        mutableStateOf(
            listOf(
                "" to "",   // Set 1
                "" to "",   // Set 2
                "" to ""    // Set 3
            )
        )
    }

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

            // CABECERA: ubicación + fecha + chip de estado + borrar
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    EstadoChip(estado = partido.estado)

                    // El creador puede borrar el partido mientras no esté finalizado
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

            // ZONA DE JUGADORES: 2 vs 2
            // Cada PosicionBox representa un hueco en el partido.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // EQUIPO 1
                Column(
                    modifier = Modifier.weight(1f),
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

                // VS CENTRADO
                Box(
                    modifier = Modifier.weight(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", style = MaterialTheme.typography.titleLarge)
                }

                // EQUIPO 2
                Column(
                    modifier = Modifier.weight(1f),
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


            // EDITOR DE RESULTADO (solo si está "jugando" y el
            // usuario actual es el creador del partido).
            if (partido.estado == "jugando" && partido.creadorId == currentUid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Introduce el resultado",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Set 1
                ResultadoRow(
                    label = "Set 1",
                    valueEq1 = setsValues[0].first,
                    onValueEq1Change = { nuevo ->
                        setsValues = setsValues.toMutableList().also {
                            it[0] = nuevo to it[0].second
                        }
                    },
                    valueEq2 = setsValues[0].second,
                    onValueEq2Change = { nuevo ->
                        setsValues = setsValues.toMutableList().also {
                            it[0] = it[0].first to nuevo
                        }
                    }
                )

                // Set 2
                ResultadoRow(
                    label = "Set 2",
                    valueEq1 = setsValues[1].first,
                    onValueEq1Change = { nuevo ->
                        setsValues = setsValues.toMutableList().also {
                            it[1] = nuevo to it[1].second
                        }
                    },
                    valueEq2 = setsValues[1].second,
                    onValueEq2Change = { nuevo ->
                        setsValues = setsValues.toMutableList().also {
                            it[1] = it[1].first to nuevo
                        }
                    }
                )

                // Set 3 (opcional)
                ResultadoRow(
                    label = "Set 3",
                    valueEq1 = setsValues[2].first,
                    onValueEq1Change = { nuevo ->
                        setsValues = setsValues.toMutableList().also {
                            it[2] = nuevo to it[2].second
                        }
                    },
                    valueEq2 = setsValues[2].second,
                    onValueEq2Change = { nuevo ->
                        setsValues = setsValues.toMutableList().also {
                            it[2] = it[2].first to nuevo
                        }
                    }
                )

                Button(
                    onClick = { onFinalizarPartido(setsValues) },
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
fun EstadoChip(estado: String) {
    val colors = MaterialTheme.colorScheme

    // Texto y colores del chip según el estado del partido
    val (texto, bg, fg) = when (estado) {
        "pendiente" -> Triple("Pendiente", colors.surfaceVariant, colors.onSurfaceVariant)
        "listo" -> Triple("Completo", colors.primary.copy(alpha = 0.15f), colors.primary)
        "jugando" -> Triple("Jugando", Color(0xFFFFF59D), Color(0xFFF57F17))
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
fun PosicionBox(
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
        // Si la posición está vacía
        if (uid.isNullOrBlank()) {
            Text(
                "Vacío",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.6f)
            )
        } else {
            // Si ya hay jugador en esa posición
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Foto de perfil si tiene URL
                if (usuario?.fotoPerfilUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = usuario.fotoPerfilUrl,
                        contentDescription = "Foto perfil",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Avatar genérico si no tiene foto
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
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }
        }
    }
}

@Composable
fun ResultadoRow(
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
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = valueEq1,
            onValueChange = { if (it.length <= 2) onValueEq1Change(it) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        )

        Text(
            "-",
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = valueEq2,
            onValueChange = { if (it.length <= 2) onValueEq2Change(it) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            placeholder = {
                Text(
                    "0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}
