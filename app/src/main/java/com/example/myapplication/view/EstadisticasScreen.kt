package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.model.PartidoFinalizado
import com.example.myapplication.model.Usuario
import com.example.myapplication.viewmodel.EstadisticasViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EstadisticasScreen(
    navController: NavHostController,
    viewModel: EstadisticasViewModel = viewModel()
) {
    val usuario by viewModel.usuario.collectAsState()
    val partidos by viewModel.partidos.collectAsState()
    val colors = MaterialTheme.colorScheme

    //ESTA ES LA CLAVE
    LaunchedEffect(usuario) {
        val uid = usuario?.uid ?: return@LaunchedEffect
        viewModel.cargarPartidosFinalizados(uid)
    }

    MainScaffold(
        navController = navController,
        isEditing = false
    ) { padding, _ ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (usuario == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@MainScaffold
            }

            // --- TARJETA DE ESTADÍSTICAS GENERALES ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Estadísticas de ${usuario!!.username}", style = MaterialTheme.typography.titleMedium)

                    Text("Partidos jugados: ${usuario!!.partidosJugados}")
                    Text("Victorias: ${usuario!!.partidosGanados}")
                    Text("Derrotas: ${usuario!!.partidosPerdidos}")

                    val total = usuario!!.partidosJugados.takeIf { it > 0 } ?: 1
                    val winrate = (usuario!!.partidosGanados * 100) / total

                    Text("Winrate: $winrate%")
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // --- HISTORIAL DE PARTIDOS ---
            Text("Historial de partidos", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(partidos) { partido ->
                    PartidoFinalizadoCard(
                        partido = partido,
                        usuariosMapa = viewModel.usuariosMapa.collectAsState().value
                    )
                }
            }
        }
    }
}


@Composable
fun PartidoFinalizadoCard(
    partido: PartidoFinalizado,
    usuariosMapa: Map<String, Usuario> // <-- Para mostrar nombres/fotos igual que Home
) {
    val colors = MaterialTheme.colorScheme
    val df = remember { SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val wins1 = partido.sets.count { it.juegosEquipo1 > it.juegosEquipo2 }
    val wins2 = partido.sets.count { it.juegosEquipo1 < it.juegosEquipo2 }

    val equipo1Gana = wins1 > wins2

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // CABECERA: ubicación + fecha
            Column {
                Text(
                    partido.ubicacion,
                    style = MaterialTheme.typography.titleMedium
                )
                partido.fecha?.toDate()?.let {
                    Text(
                        df.format(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // EQUIPOS VISUAL AL ESTILO HOME
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                // EQUIPO 1
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PosicionBoxFinalizado(
                        usuario = usuariosMapa[partido.posiciones[0]],
                        ganador = equipo1Gana
                    )
                    PosicionBoxFinalizado(
                        usuario = usuariosMapa[partido.posiciones[1]],
                        ganador = equipo1Gana
                    )
                }

                Text("VS", style = MaterialTheme.typography.titleMedium)

                // EQUIPO 2
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PosicionBoxFinalizado(
                        usuario = usuariosMapa[partido.posiciones[2]],
                        ganador = !equipo1Gana
                    )
                    PosicionBoxFinalizado(
                        usuario = usuariosMapa[partido.posiciones[3]],
                        ganador = !equipo1Gana
                    )
                }
            }

            // RESULTADO FINAL
            if (partido.sets.isNotEmpty()) {
                val textoSets = partido.sets.joinToString("  ·  ") {
                    "${it.juegosEquipo1}-${it.juegosEquipo2}"
                }

                Text(
                    text = "Resultado: $textoSets",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


@Composable
private fun PosicionBoxFinalizado(
    usuario: Usuario?,
    ganador: Boolean
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (ganador) colors.primary.copy(alpha = 0.18f)
                else colors.surfaceVariant
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        if (usuario == null) {
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

                if (!usuario.fotoPerfilUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = usuario.fotoPerfilUrl,
                        contentDescription = null,
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
                    usuario.username,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

