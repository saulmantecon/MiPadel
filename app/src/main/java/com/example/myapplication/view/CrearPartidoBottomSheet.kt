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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.CrearPartidoViewModel
import java.util.Calendar

/**
 * BottomSheet que contiene el formulario de creación de un partido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPartidoBottomSheet(
    viewModel: CrearPartidoViewModel,
    onCerrar: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    // Estados expuestos por el ViewModel
    val ubicacion by viewModel.ubicacion.collectAsState()
    val fecha by viewModel.fecha.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val context = LocalContext.current

    // Calendar usado para inicializar DatePicker
    val calendar = remember { Calendar.getInstance() }

    /**
     * DatePicker + TimePicker:
     */
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->

                // Guardamos día seleccionado temporalmente
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                // Tras seleccionar fecha -> se abre selector de hora
                TimePickerDialog(
                    context,
                    { _, hour, minute ->

                        val cal = Calendar.getInstance()
                        cal.set(year, month, day, hour, minute, 0)

                        // Enviamos la fecha final al ViewModel
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

            //ubicación
            OutlinedTextField(
                value = ubicacion,
                onValueChange = viewModel::setUbicacion,
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth()
            )

            val formatter = remember {
                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            }

            //Botón para seleccionar fecha y hora
            Button(
                onClick = { datePickerDialog.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    fecha?.toDate()?.let { formatter.format(it) }
                        ?: "Elegir fecha y hora"
                )
            }

            //Botón final para crear el partido
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
