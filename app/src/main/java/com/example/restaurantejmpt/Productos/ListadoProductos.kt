package com.example.restaurantejmpt.Productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Model.Producto
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Rutas.Rutas

@Composable
fun ListadoProductos(
    productoViewModel: ProductoViewModel,
    navController: NavHostController,
    personaViewModel: PersonaViewModel
) {
    //Obtiene el rol con el que se ha iniciado sesión
    val rol by personaViewModel.currentRole.collectAsState()

    //Booleano que nos indicará si tendrá los permisos de edición o no
    val isAdmin = rol == Rol.ADMIN

    LaunchedEffect(Unit) {
        productoViewModel.loadProductos()
    }

    val todosLosProductos = productoViewModel.productos

    // Filtramos las listas por tipo
    val platos = todosLosProductos.filter { it.tipo.lowercase() == "plato" }
    val bebidas = todosLosProductos.filter { it.tipo.lowercase() == "bebida" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Catálogo de productos",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEAEAF2))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- SECCIÓN PLATOS ---
            if (platos.isNotEmpty()) {
                item {
                    SeccionHeader(titulo = "PLATOS")
                }
                items(platos) { producto ->
                    ProductoCard(
                        producto = producto,
                        onEditar = {
                            productoViewModel.seleccionarProducto(producto)
                            navController.navigate(Rutas.UPDATE_PRODUCTO)
                        },
                        onEliminar = { productoViewModel.borrarProducto(producto.id) },
                        isAdmin
                    )
                }
            }

            // Espacio entre secciones
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // --- SECCIÓN BEBIDAS ---
            if (bebidas.isNotEmpty()) {
                item {
                    SeccionHeader(titulo = "BEBIDAS")
                }
                items(bebidas) { producto ->
                    ProductoCard(
                        producto = producto,
                        onEditar = {
                            productoViewModel.seleccionarProducto(producto)
                            navController.navigate(Rutas.UPDATE_PRODUCTO)
                        },
                        onEliminar = { productoViewModel.borrarProducto(producto.id) },
                        isAdmin
                    )
                }
            }

            // Caso por si no hay nada en ninguna categoría
            if (todosLosProductos.isEmpty()) {
                item {
                    Text(
                        "No hay productos registrados",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionHeader(titulo: String) {
    Text(
        text = titulo,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF3F51B5),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
    HorizontalDivider(thickness = 2.dp, color = Color(0xFF3F51B5))
}
@Composable
fun ProductoCard(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    isAdmin: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDADAE3)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = producto.nombre,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Tipo: ${producto.tipo}")
            Text(text = "Descripción: ${producto.descripcion}")
            Text(text = "Precio: ${producto.precio} €")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (producto.imagenUrl.isBlank())
                    "Imagen: Genérica"
                else
                    "Imagen: Disponible"
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(4.dp))

            if (isAdmin) {
                HorizontalDivider()

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, "Editar")
                    }

                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, "Eliminar")
                    }
                }
            }
        }
    }
}