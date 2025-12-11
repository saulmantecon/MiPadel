package com.example.myapplication.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

/**
 * Barra superior (TopAppBar) dinámica.
 *
 * Muestra:
 * - Menú lateral (hamburger)
 * - Botón atrás
 * - Botón añadir
 * - Botón buscar
 * - Botón editar/guardar
 *
 * Su comportamiento depende de lo que defina cada pantalla (Screen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = false,
    showMenu: Boolean = false,
    showAdd: Boolean = false,
    showSearch: Boolean = false,
    showEdit: Boolean = false,
    isEditing: Boolean = false,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    CenterAlignedTopAppBar(

        // TÍTULO
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        },

        // ICONO DE NAVEGACIÓN (menú o atrás)
        navigationIcon = {
            when {
                showBack -> {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = colors.onSurface
                        )
                    }
                }
                showMenu -> {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menú",
                            tint = colors.onSurface
                        )
                    }
                }
            }
        },

        // ACCIONES (add, search, edit)
        actions = {

            if (showAdd) {
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Añadir",
                        tint = colors.onSurface
                    )
                }
            }

            if (showSearch) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar",
                        tint = colors.onSurface
                    )
                }
            }

            if (showEdit) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Guardar" else "Editar",
                        tint = if (isEditing) colors.primary else colors.onSurface
                    )
                }
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            titleContentColor = colors.onSurface
        )
    )
}
