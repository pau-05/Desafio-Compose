package com.example.restaurantejmpt.Productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.restaurantejmpt.Admin.AdminViewModel

@Composable
fun ModificarProducto(
    adminViewModel: AdminViewModel
) {
    val producto by adminViewModel.productoSeleccionado.collectAsState()

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
    var imagenUrl by remember { mutableStateOf(producto!!.imagenUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "Modificar Producto")

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

        OutlinedTextField(
            value = imagenUrl,
            onValueChange = { imagenUrl = it },
            label = { Text("Imagen (String / URL opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

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
                adminViewModel.actualizarProducto(
                    producto!!.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio.toDoubleOrNull() ?: 0.0,
                        tipo = tipo,
                        imagenUrl = imagenUrl
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar producto")
        }
    }
}