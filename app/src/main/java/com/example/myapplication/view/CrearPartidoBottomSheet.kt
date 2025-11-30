package com.example.myapplication.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.CrearPartidoViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPartidoBottomSheet(
    viewModel: CrearPartidoViewModel,
    onCerrar: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val colors = MaterialTheme.colorScheme

    val ubicacion by viewModel.ubicacion.collectAsState()
    val fecha by viewModel.fecha.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val calendar = remember { Calendar.getInstance() }

    // DatePicker -> cuando se selecciona date, se abre TimePicker automáticamente
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    context,
                    { _, hour, minute ->

                        val cal = Calendar.getInstance() // <-- HORA LOCAL

                        // Usamos el día/mes/año que se seleccionó antes
                        cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                        cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                        cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))

                        // Asignamos la hora seleccionada REAL
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)

                        // Creamos timestamp exactamente con esa hora local
                        viewModel.setFecha(
                            com.google.firebase.Timestamp(cal.time)
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()


            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Mostrar snackbar + cerrar si va bien
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            scope.launch {
                snackbarHostState.showSnackbar(mensaje!!)
            }
            if (mensaje == "Partido creado correctamente") {
                viewModel.resetForm()
                onCerrar()
            }
            viewModel.limpiarMensaje()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = colors.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                "Crear partido",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )

            // Campo ubicación
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { viewModel.setUbicacion(it) },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth()
            )
            val formatter = remember {
                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            }
            // Botón elegir fecha/hora
            Button(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = fecha?.toDate()?.let { formatter.format(it) } ?: "Elegir fecha y hora"
                )
            }

            // Botón crear partido
            Button(
                onClick = { viewModel.crearPartido() },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = colors.onPrimary
                    )
                } else {
                    Text("Crear partido")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}