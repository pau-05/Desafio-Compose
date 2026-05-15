package com.example.restaurantejmpt.Barman

import android.annotation.SuppressLint
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.restaurantejmpt.Camarero.ComandaViewModel
import com.example.restaurantejmpt.Model.Comanda
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Rutas.Rutas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoComandasScreen(
    comandaViewModel: ComandaViewModel,
    personaViewModel: PersonaViewModel,
    navController: NavController
) {
    // Obtiene el rol con el que se ha iniciado sesión
    val rol by personaViewModel.currentRole.collectAsState()

    // Booleano que nos indicará si tendrá los permisos de edición o no
    val isCamarero = rol == Rol.CAMARERO

    val todasLasComandas by comandaViewModel.comandas.collectAsState()
    val comandas = todasLasComandas.filter { !it.servido }
    val snackbarHostState = remember { SnackbarHostState() }

    // Para que cada vez que se venga a esta screen, no se guarde la que estaba en edición
    comandaViewModel.limpiarComandaSeleccionada()

    var comandaAEliminar by remember { mutableStateOf<Comanda?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comandas Pendientes") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (comandas.isNotEmpty()) {
                                Badge { Text(comandas.size.toString()) }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = "Comandas")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (comandas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay comandas pendientes", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comandas) { comanda ->
                    ComandaCard(
                        comanda = comanda,
                        onServirClick = {
                            comandaViewModel.marcarComoServida(comanda)
                        },
                        onClick = {
                            // Navegar a la edición de la comanda solo si es camarero
                            if (isCamarero && !comanda.servido) {
                                comandaViewModel.seleccionarComanda(comanda)
                                navController.navigate(Rutas.CREAR_COMANDA)
                            }
                        },
                        onLongClick = {
                            // Click largo -> borrar (solo si es camarero)
                            if (isCamarero) {
                                comandaAEliminar = comanda
                            }
                        },
                        mostrarBotonBorrar = isCamarero // Conexión de permiso para la UI
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    comandaAEliminar?.let { comanda ->
        AlertDialog(
            onDismissRequest = { comandaAEliminar = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Eliminar la comanda?") },
            confirmButton = {
                Button(onClick = {
                    comandaViewModel.deleteComanda(comanda.id)
                    comandaAEliminar = null
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                Button(onClick = {
                    comandaAEliminar = null
                }) {
                    Text("No")
                }
            }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ComandaCard(
    comanda: Comanda,
    onServirClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    mostrarBotonBorrar: Boolean // Controla la visibilidad del botón rojo
) {
    val esServida = comanda.servido

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esServida) Color(0xFFE0E0E0) else Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
            ) {
                // Cabecera: ID y Fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Comanda #${comanda.id.takeLast(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = comanda.fechaHora.toString(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Listado de Productos
                comanda.productos.forEach { producto ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${producto.cantidad}x ${producto.nombre}")
                        Text("${producto.subtotal} €")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila del Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total:", fontWeight = FontWeight.Bold)
                    Text(
                        "${comanda.total} €",
                        fontWeight = FontWeight.Bold,
                        color = if (esServida) Color.DarkGray else Color(0xFF4CAF50)
                    )
                }

                // Información de Ubicación
                if (comanda.ubicacionLat != 0.0 || comanda.ubicacionLng != 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lat: ${String.format("%.4f", comanda.ubicacionLat)}, Lng: ${String.format("%.4f", comanda.ubicacionLng)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Acciones de la comanda
            if (!esServida) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Servir: Se expande si no hay botón de borrar
                    Button(
                        onClick = onServirClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Servir")
                    }

                    // Botón Borrar: Solo aparece si el rol es Camarero
                    if (mostrarBotonBorrar) {
                        Button(
                            onClick = onLongClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            modifier = Modifier.width(60.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Borrar comanda",
                                tint = Color.White
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✔ Servida",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}