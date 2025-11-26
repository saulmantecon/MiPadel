package com.example.myapplication.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.viewmodel.EstadisticasViewModel

@Composable
fun EstadisticasScreen(
    navController: NavHostController,
    viewModel: EstadisticasViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val usuario by viewModel.usuario.collectAsState()
    val partidos by viewModel.partidos.collectAsState()

    MainScaffold(
        navController = navController,
        isEditing = false
    ) { padding, snackbarHostState ->

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
                    Text("Resumen de ${usuario!!.username}", style = MaterialTheme.typography.titleMedium)

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
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Ubicación: ${partido.ubicacion}")
                            Text("Nivel: ${partido.nivel}")
                           // Text("Jugadores: ${partido.jugadores.size}/${partido.maxJugadores}")
                            Text("Fecha: ${partido.fecha?.toDate()}")
                        }
                    }
                }
            }
        }
    }
}
