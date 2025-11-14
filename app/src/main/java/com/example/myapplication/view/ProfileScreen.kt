package com.example.myapplication.view

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.viewmodel.ProfileViewModel
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val usuario by viewModel.usuario.collectAsState()

    var editMode by remember { mutableStateOf(false) }
    var hasEdited by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var username by remember { mutableStateOf(usuario?.username ?: "") }

    // URIs para la imagen (foto nueva o temporal de cámara)
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // diálogo de cámara/galería
    var showDialog by remember { mutableStateOf(false) }

    // Launchers para cámara y galería
    // recibe un URI donde se guardará la foto
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            fotoUri = tempPhotoUri
            hasEdited = true
        }
    }
    // devuelve un URI del contenido seleccionado
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            fotoUri = uri
            hasEdited = true
        }
    }

    MainScaffold(
        navController = navController,
        isEditing = editMode,
        onEditClick = {
            // Cambia entre modo edición y confirmación
            if (editMode) hasEdited = true
            editMode = !editMode
        }
    ) { padding, snackbarHostState ->

        if (usuario == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@MainScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Imagen de perfil
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                //circulo con imagen o placeholder
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceVariant.copy(alpha = 0.3f),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp
                ) {
                    if (fotoUri != null || usuario?.fotoPerfilUrl != null) {
                        //imagen de perfil del usuario
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(fotoUri ?: usuario?.fotoPerfilUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        // Placeholder cuando no hay imagen
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Sin foto",
                                tint = colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }
                }

                // Botón para cambiar imagen, solo aparece si esta editando
                if (editMode) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .offset(x = (-6).dp, y = (-6).dp)
                            .size(42.dp)
                            .background(colors.primary, CircleShape)
                            .border(2.dp, colors.background, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Cambiar foto",
                            tint = colors.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Campos de texto
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre y apellidos") },
                enabled = editMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                enabled = editMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            // Efecto: guardar cambios al confirmar (tick)
            LaunchedEffect(editMode) {
                if (!editMode && hasEdited) {
                    usuario?.let {
                        val actualizado = it.copy(
                            nombre = nombre.trim(),
                            username = username.trim()
                        )
                        val message = viewModel.updateUsuarioCompleto(context, actualizado, fotoUri)
                        snackbarHostState.showSnackbar(message)
                        hasEdited = false
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = colors.surfaceVariant.copy(alpha = 0.98f),
                title = {
                    Text(
                        "Seleccionar imagen",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                showDialog = false
                                pickPhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.AccountBox, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Elegir desde galería")
                        }
                        Button(
                            onClick = {
                                showDialog = false
                                tempPhotoUri = createImageUri(context)
                                takePhotoLauncher.launch(tempPhotoUri)
                            },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tomar una foto")
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

/** Crea una URI temporal para guardar la foto de la cámara **/
fun createImageUri(context: Context): Uri {
    val file = File.createTempFile("profile_photo_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}
