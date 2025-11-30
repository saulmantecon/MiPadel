package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.model.PartidoFinalizado
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
                    PartidoFinalizadoCard(partido = partido)
                }
            }
        }
    }
}

@Composable
fun PartidoFinalizadoCard(
    partido: PartidoFinalizado
) {
    val colors = MaterialTheme.colorScheme
    val df = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // Definir ganador por sets
    val wins1 = partido.sets.count { it.juegosEquipo1 > it.juegosEquipo2 }
    val wins2 = partido.sets.count { it.juegosEquipo1 < it.juegosEquipo2 }

    val equipo1Gana = wins1 > wins2

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Fecha y ubicación
            Column {
                Text(partido.ubicacion, style = MaterialTheme.typography.titleMedium)
                partido.fecha?.toDate()?.let {
                    Text(df.format(it), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Equipos VS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Equipo 1
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (equipo1Gana) colors.primary.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Equipo 1", style = MaterialTheme.typography.labelMedium)
                    Text(partido.posiciones[0], style = MaterialTheme.typography.bodySmall)
                    Text(partido.posiciones[1], style = MaterialTheme.typography.bodySmall)
                }

                Text("VS", modifier = Modifier.padding(horizontal = 8.dp))

                // Equipo 2
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!equipo1Gana) colors.primary.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Equipo 2", style = MaterialTheme.typography.labelMedium)
                    Text(partido.posiciones[2], style = MaterialTheme.typography.bodySmall)
                    Text(partido.posiciones[3], style = MaterialTheme.typography.bodySmall)
                }
            }

            // Resultado por sets
            if (partido.sets.isNotEmpty()) {
                val textoSets = partido.sets.joinToString("  |  ") {
                    "${it.juegosEquipo1}-${it.juegosEquipo2}"
                }

                Text(
                    "Resultado: $textoSets",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
