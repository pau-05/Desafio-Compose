package com.example.restaurantejmpt.Productos

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip

@Composable
fun FormularioProducto(
    productoViewModel: ProductoViewModel
) {

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("plato") }

    // Aquí guardaremos la URI
    var imagenUrl by remember { mutableStateOf("") }


    // Launcher GALERÍA
    val launcherGaleria =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            uri?.let {
                imagenUrl = it.toString()
            }
        }

    // Launcher CÁMARA
    val launcherCamara =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            // Esta opción devuelve Bitmap
            // Para simplificar:
            // usa mejor galería si quieres persistencia
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(text = "Añadir Producto")

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

        if (imagenUrl.isNotEmpty()) {

            AsyncImage(
                model = imagenUrl,
                contentDescription = "Imagen seleccionada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // BOTONES
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = {
                    launcherGaleria.launch("image/*")
                }
            ) {
                Text("Galería")
            }

            Button(
                onClick = {
                    launcherCamara.launch(null)
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

        Button(
            onClick = {

                productoViewModel.agregarProducto(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio.toDoubleOrNull() ?: 0.0,
                    tipo = tipo,
                    imagenUrl = imagenUrl
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar producto")
        }
    }
}