package com.example.myapplication.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavHostController,
    onVerEstadisticas: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    // Usuario actual desde CurrentUserManager
    val usuario by viewModel.usuario.collectAsState()

    // Modo edición controlado por el icono del AppBar (tick)
    var editMode by remember { mutableStateOf(false) }

    // Indica si el usuario ha cambiado algo realmente
    var hasEdited by remember { mutableStateOf(false) }

    // Campos editables
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var username by remember { mutableStateOf(usuario?.username ?: "") }

    // Imagen seleccionada, si existe
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Diálogo para elegir entre cámara o galería
    var showDialog by remember { mutableStateOf(false) }

    // CoroutineScope para poder ejecutar funciones suspend dentro del contenido
    val scope = rememberCoroutineScope()

    //LAUNCHERS:

    // Cámara
    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            fotoUri = tempPhotoUri
            hasEdited = true
        }
    }

    // Permiso cámara
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            tempPhotoUri = createImageUri(context)
            takePhotoLauncher.launch(tempPhotoUri)
        }
    }

    // Galería
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
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
            // Cambiar de modo edición
            editMode = !editMode
        }
    ) { padding, snackbarHostState ->

        // Guardar los datos solo cuando:
        // - Se sale del modo edición (editMode = false)
        // - El usuario ha cambiado algo
        // - Usuario cargado
        LaunchedEffect(editMode) {
            if (!editMode && hasEdited && usuario != null) {

                val actualizado = usuario!!.copy(
                    nombre = nombre.trim(),
                    username = username.trim()
                )

                // Llamada suspend dentro de una corrutina
                scope.launch {
                    val msg = viewModel.updateUsuarioCompleto(
                        context,
                        actualizado,
                        fotoUri
                    )
                    snackbarHostState.showSnackbar(msg)
                }

                hasEdited = false
            }
        }

        //CARGA:
        if (usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@MainScaffold
        }

        //Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // FOTO DE PERFIL
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceVariant.copy(alpha = 0.3f),
                    tonalElevation = 6.dp
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoUri ?: usuario?.fotoPerfilUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                    )
                }

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

            //Textfields
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; hasEdited = true },
                label = { Text("Nombre y apellidos") },
                enabled = editMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; hasEdited = true },
                label = { Text("Nombre de usuario") },
                enabled = editMode,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            // BOTÓN ESTADÍSTICAS
            if (!editMode) {
                Button(
                    onClick = onVerEstadisticas,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(colors.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Ver estadísticas", color = colors.onPrimary)
                }
            }
        }

        //DIÁLOGO DE FOTO
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "Seleccionar imagen",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                showDialog = false
                                pickPhotoLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.AccountBox, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Elegir desde galería")
                        }

                        Button(
                            onClick = {
                                showDialog = false

                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (granted) {
                                    tempPhotoUri = createImageUri(context)
                                    takePhotoLauncher.launch(tempPhotoUri)
                                } else {
                                    requestCameraPermissionLauncher.launch(
                                        Manifest.permission.CAMERA
                                    )
                                }
                            },
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.Person, null)
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

/** Crea un archivo temporal para recibir la foto tomada con la cámara */
fun createImageUri(context: Context): Uri {
    val file = File.createTempFile("profile_photo_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}
