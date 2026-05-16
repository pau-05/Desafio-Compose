package com.example.restaurantejmpt.Productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import android.util.Log
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FormularioProducto(
    productoViewModel: ProductoViewModel
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("plato") }

    // URI para la imagen seleccionada (vista previa)
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //Función para crear archivo temporal
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    // Archivo temporal (inicializado bajo demanda)
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    // Launcher GALERÍA
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
        Log.d("Formulario", "Imagen de galería: $uri")
    }

    // Launcher CÁMARA
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentPhotoFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.example.restaurantejmpt.fileprovider",
                    file
                )
                imagenUri = uri
                Log.d("Formulario", "Foto de cámara: $uri")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Añadir Producto", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth()
        )

        // Vista previa de la imagen seleccionada
        if (imagenUri != null) {
            AsyncImage(
                model = imagenUri,
                contentDescription = "Imagen seleccionada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin imagen seleccionada", color = Color.Gray)
            }
        }

        // BOTONES
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { launcherGaleria.launch("image/*") }) {
                Text("Galería")
            }

            Button(
                onClick = {
                    try {
                        // Crear archivo temporal SOLO cuando se pulsa el botón
                        val photoFile = createImageFile()
                        currentPhotoFile = photoFile
                        val uri = FileProvider.getUriForFile(
                            context,
                            "com.example.restaurantejmpt.fileprovider",
                            photoFile
                        )
                        launcherCamara.launch(uri)
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                        Log.e("Formulario", "Error al abrir cámara", e)
                    }
                }
            ) {
                Text("Cámara")
            }
        }

        Text(text = "Tipo de producto")

        Row {
            Row {
                RadioButton(
                    selected = tipo == "plato",
                    onClick = { tipo = "plato" }
                )
                Text("Plato")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row {
                RadioButton(
                    selected = tipo == "bebida",
                    onClick = { tipo = "bebida" }
                )
                Text("Bebida")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón guardar
        Button(
            onClick = {
                if (nombre.isBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("El nombre es obligatorio")
                    }
                    return@Button
                }
                if (precio.toDoubleOrNull() == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Precio inválido")
                    }
                    return@Button
                }
                if (imagenUri == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Selecciona una imagen")
                    }
                    return@Button
                }

                isUploading = true

                productoViewModel.subirImagenYGuardarProducto(
                    imageUri = imagenUri!!,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio.toDouble(),
                    tipo = tipo,
                    onSuccess = {
                        isUploading = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Producto guardado correctamente")
                        }
                        // Limpiar formulario
                        nombre = ""
                        descripcion = ""
                        precio = ""
                        imagenUri = null
                        currentPhotoFile = null
                    },
                    onError = { error ->
                        isUploading = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Error: $error")
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subiendo imagen...")
            } else {
                Text("Guardar producto")
            }
        }

        // Snackbar para mensajes
        SnackbarHost(hostState = snackbarHostState)
    }
}