package com.example.restaurantejmpt.Camarero

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restaurantejmpt.Model.Comanda
import com.example.restaurantejmpt.Model.Producto
import com.example.restaurantejmpt.Model.ProductoComanda
import com.example.restaurantejmpt.Productos.ProductoViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

private const val TAG = "CrearComandaScreen"

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearComandaScreen(
    camareroId: String,
    ubicacionViewModel: MapsViewModel,
    productoViewModel: ProductoViewModel,
    comandaViewModel: ComandaViewModel,
    onNavigateToMapa: () -> Unit
) {

    //Estados
    var seleccion by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    val isLoadingCatalogo by productoViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //Observar ubicación seleccionada desde el ViewModel
    val ubicacion by ubicacionViewModel.ubicacionSeleccionada.collectAsState()

    //Cargar productos
    LaunchedEffect(Unit) {
        productoViewModel.loadProductos()
    }
    //Variable donde se van a guardar los productos
    val productosCatalogo = productoViewModel.productos

    //Función para crear comanda
    fun crearComanda() {
        scope.launch {
            //Validaciones
            if (seleccion.isEmpty()) {
                snackbarHostState.showSnackbar("Selecciona al menos un producto")
                return@launch
            }
            if (ubicacion == null) {
                snackbarHostState.showSnackbar("Selecciona una ubicación en el mapa")
                return@launch
            }

            isLoading = true

            try {
                //Construir lista de productos de la comanda
                val productosComanda = seleccion.mapNotNull { (productoId, cantidad) ->
                    val producto = productosCatalogo.find { it.id == productoId }
                    producto?.let {
                        ProductoComanda(
                            productoId = productoId,
                            nombre = producto.nombre,
                            cantidad = cantidad,
                            precioUnitario = producto.precio,
                            subtotal = producto.precio * cantidad
                        )
                    }
                }

                val total = productosComanda.sumOf { it.subtotal }

                val comanda = Comanda(
                    servido = false,
                    fechaHora = System.currentTimeMillis(),
                    ubicacionLat = ubicacion!!.latitude,
                    ubicacionLng = ubicacion!!.longitude,
                    productos = productosComanda,
                    total = total,
                    camareroId = camareroId
                )

                //Guardar en Realtime Firebase
                comandaViewModel.sendComanda(comanda)

                //Limpiar selección y ubicación
                seleccion = emptyMap()
                ubicacionViewModel.limpiarUbicacion()

            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Comanda") },
                actions = {
                    //Badge con número de productos seleccionados
                    BadgedBox(
                        badge = {
                            val totalItems = seleccion.values.sum()
                            if (totalItems > 0) {
                                Badge { Text(totalItems.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            //Título sección productos
            Text(
                text = "Productos disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            //Lista de productos o loading
            when {
                isLoadingCatalogo -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cargando productos...")
                        }
                    }
                }
                productosCatalogo.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay productos disponibles")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(productosCatalogo) { producto ->
                            ProductoItem(
                                producto = producto,
                                cantidad = seleccion[producto.id] ?: 0,
                                onCantidadChange = { nuevaCantidad ->
                                    seleccion = if (nuevaCantidad <= 0) {
                                        seleccion.minus(producto.id)
                                    } else {
                                        seleccion + (producto.id to nuevaCantidad)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Tarjeta de ubicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ubicacion de la comanda", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (ubicacion != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        tint = Color.Green,
                                        modifier = Modifier.size(16.dp),
                                        contentDescription = "mesa"
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mesa seleccionada", color = Color.Green)
                                }
                                Text(
                                    text = "Lat: ${String.format("%.4f", ubicacion!!.latitude)}, Lng: ${String.format("%.4f", ubicacion!!.longitude)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            TextButton(onClick = onNavigateToMapa) {
                                Text("Cambiar")
                            }
                        }
                    } else {
                        Button(
                            onClick = onNavigateToMapa,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar ubicación en mapa")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Resumen de selección
            if (seleccion.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Resumen del pedido", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val totalItems = seleccion.values.sum()
                        val totalPrecio = seleccion.mapNotNull { (id, cantidad) ->
                            productosCatalogo.find { it.id == id }?.precio?.times(cantidad)
                        }.sum()
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Productos: $totalItems unidades")
                            Text("Total: ${String.format("%.2f", totalPrecio)} €")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            //Botón crear comanda
            Button(
                onClick = { crearComanda() },
                modifier = Modifier.fillMaxWidth(),
                enabled = seleccion.isNotEmpty() && ubicacion != null && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear comanda")
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ProductoItem(
    producto: Producto,
    cantidad: Int,
    onCantidadChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    //Icono según el tipo de producto
                    Icon(
                        imageVector = when (producto.tipo.lowercase()) {
                            "plato" -> Icons.Rounded.FavoriteBorder
                            "bebida" -> Icons.Outlined.Star
                            else -> Icons.Outlined.Menu
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = producto.nombre,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                if (producto.descripcion.isNotEmpty()) {
                    Text(
                        text = producto.descripcion,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${String.format("%.2f", producto.precio)} €",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            //Controles de cantidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (cantidad > 0) onCantidadChange(cantidad - 1) },
                    enabled = cantidad > 0
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar")
                }

                Text(
                    text = cantidad.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onCantidadChange(cantidad + 1) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        }
    }
}