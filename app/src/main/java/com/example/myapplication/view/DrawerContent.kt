package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SettingsViewModel

/**
 * DrawerContent
 *
 * Contenido del menú lateral de la aplicación.
 * Permite cambiar el tema de la app y cerrar sesión.
 * La estructura está organizada verticalmente usando un Column.
 */
@Composable
fun DrawerContent(
    onLogoutClick: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val colors = MaterialTheme.colorScheme
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(270.dp) // ancho estándar de un Drawer
            .background(colors.surface)
            .padding(horizontal = 20.dp)
    ) {
        // Margen superior para no pegar “Menú” al borde del Drawer
        Spacer(modifier = Modifier.height(40.dp))

        // Título de la sección
        Text(
            "Menú",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider() // línea separadora

        Spacer(modifier = Modifier.height(16.dp))

        // Selector desplegable para cambiar el tema (claro / oscuro / sistema)
        ThemeDropdownSelector(
            selected = themeMode,
            onSelected = { mode ->
                // Guardamos el nuevo modo en DataStore
                settingsViewModel.setTheme(mode)
            }
        )

        // Separador entre sección superior y botón inferior
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Ocupa el espacio disponible y empuja el botón de logout hacia abajo
        Spacer(modifier = Modifier.weight(1f))

        // Botón para cerrar sesión (diseñado como fila clicable con fondo)
        DrawerButton(
            text = "Cerrar sesión",
            onClick = onLogoutClick,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            textColor = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * ThemeDropdownSelector
 *
 * Campo de texto con menú desplegable para elegir el tema de la app.
 * Usa ExposedDropdownMenuBox, que enlaza automáticamente el TextField
 * con el menú y mantiene ambos alineados y del mismo ancho.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDropdownSelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Opciones disponibles de tema: clave → etiqueta visible
    val options = listOf(
        "system" to "Sistema",
        "light" to "Modo claro",
        "dark" to "Modo oscuro"
    )

    // Etiqueta que se muestra según el valor actual
    val selectedLabel = options.first { it.first == selected }.second

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded } // abre/cierra el menú al pulsar cualquier parte del TextField
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tema") },
            trailingIcon = {
                // Icono estándar de desplegable de Material3
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable) // ancla el menú debajo del textfield
                .fillMaxWidth()
        )

        // Menú que aparece bajo el TextField con el mismo ancho
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(key) // enviamos el nuevo tema
                        expanded = false // cerramos el menú
                    }
                )
            }
        }
    }
}

/**
 * DrawerButton
 *
 * Botón simple estilizado para usar dentro del Drawer.
 * Es una fila clicable con fondo suave y texto destacado.
 */
@Composable
fun DrawerButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)) // esquinas redondeadas
            .background(color)               // fondo suave
            .clickable { onClick() }         // acción al pulsar
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = textColor, style = MaterialTheme.typography.bodyLarge)
    }
}
