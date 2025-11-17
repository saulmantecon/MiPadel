package com.example.myapplication.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SettingsViewModel

@Composable
fun DrawerContent(
    onCloseDrawer: () -> Unit,
    onLogoutClick: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val colors = MaterialTheme.colorScheme
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(colors.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("Menú", style = MaterialTheme.typography.titleLarge)

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Text("Apariencia", style = MaterialTheme.typography.titleMedium)

        DrawerThemeOption(
            text = "Modo claro",
            selected = themeMode == "light",
            onClick = {
                settingsViewModel.setTheme("light")
                onCloseDrawer()
            }
        )

        DrawerThemeOption(
            text = "Modo oscuro",
            selected = themeMode == "dark",
            onClick = {
                settingsViewModel.setTheme("dark")
                onCloseDrawer()
            }
        )

        DrawerThemeOption(
            text = "Seguir sistema",
            selected = themeMode == "system",
            onClick = {
                settingsViewModel.setTheme("system")
                onCloseDrawer()
            }
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Text(
            "Cerrar sesión",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogoutClick() }
                .padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "Cerrar menú",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCloseDrawer() }
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = colors.primary
        )
    }
}


@Composable
fun DrawerThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.primary
            )
        }
    }
}
