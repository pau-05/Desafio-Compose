package com.example.restaurantejmpt.Productos

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun ModificarProducto(
    productoViewModel: ProductoViewModel
) {
    val context = LocalContext.current

    val producto by productoViewModel.productoSeleccionado.collectAsState()

    if (producto == null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("No hay producto seleccionado")
        }
        return
    }

    var nombre by remember { mutableStateOf(producto!!.nombre) }
    var descripcion by remember { mutableStateOf(producto!!.descripcion) }
    var precio by remember { mutableStateOf(producto!!.precio.toString()) }
    var tipo by remember { mutableStateOf(producto!!.tipo) }

    // URI para la NUEVA imagen seleccionada (solo si el usuario elige una nueva)
    var nuevaImagenUri by remember { mutableStateOf<Uri?>(null) }
    // Bandera para saber si se cambió la imagen
    var imagenCambiada by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Función para crear archivo temporal
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
        nuevaImagenUri = uri
        imagenCambiada = true
        Log.d("ModificarProducto", "Nueva imagen de galería: $uri")
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
                nuevaImagenUri = uri
                imagenCambiada = true
                Log.d("ModificarProducto", "Nueva foto de cámara: $uri")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Modificar Producto", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

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

        //Mostrar imagen ACTUAL o la NUEVA seleccionada
        val imagenAMostrar = if (imagenCambiada && nuevaImagenUri != null) {
            nuevaImagenUri
        } else {
            // Si no hay nueva imagen, mostrar la existente (si tiene URL)
            if (producto!!.imagenUrl.isNotEmpty()) {
                Uri.parse(producto!!.imagenUrl)
            } else {
                null
            }
        }

        if (imagenAMostrar != null) {
            AsyncImage(
                model = imagenAMostrar,
                contentDescription = "Imagen del producto",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder cuando no hay imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin imagen", color = Color.Gray)
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
                        Log.e("ModificarProducto", "Error al abrir cámara", e)
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

                isUploading = true

                if (imagenCambiada && nuevaImagenUri != null) {
                    //Hay nueva imagen -> subir a Storage y actualizar
                    productoViewModel.actualizarProductoConImagen(
                        productoId = producto!!.id,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio.toDouble(),
                        tipo = tipo,
                        nuevaImagenUri = nuevaImagenUri!!,
                        imagenActualUrl = producto!!.imagenUrl,
                        onSuccess = {
                            isUploading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Producto actualizado correctamente")
                            }
                        },
                        onError = { error ->
                            isUploading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: $error")
                            }
                        }
                    )
                } else {
                    //Sin nueva imagen -> solo actualizar datos
                    productoViewModel.actualizarProductoSinImagen(
                        productoId = producto!!.id,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio.toDouble(),
                        tipo = tipo,
                        onSuccess = {
                            isUploading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Producto actualizado correctamente")
                            }
                        },
                        onError = { error ->
                            isUploading = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: $error")
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardando...")
            } else {
                Text("Actualizar producto")
            }
        }

        // Snackbar para mensajes
        SnackbarHost(hostState = snackbarHostState)
    }
}