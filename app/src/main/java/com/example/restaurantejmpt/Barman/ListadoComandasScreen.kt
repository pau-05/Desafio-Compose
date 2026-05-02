package com.example.restaurantejmpt.Barman

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.restaurantejmpt.Model.Usuario
import com.example.restaurantejmpt.Rutas.Rutas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoComandasScreen(
    comandaViewModel: ComandaViewModel,
    personaViewModel: PersonaViewModel,
    navController: NavController
) {
    //Obtiene el rol con el que se ha iniciado sesión
    val rol by personaViewModel.currentRole.collectAsState()

    //Booleano que nos indicará si tendrá los permisos de edición o no
    val isCamarero = rol == Rol.CAMARERO

    val todasLasComandas by comandaViewModel.comandas.collectAsState()
    val comandas = todasLasComandas.filter { !it.servido }
    val snackbarHostState = remember { SnackbarHostState() }

    //Para que cada vez que se venga a esta screen, no se guarde la que estaba en edición
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
                            //Navegar a la edición de la comanda solo si es camarero
                            if (isCamarero) {
                                comandaViewModel.seleccionarComanda(comanda)
                                navController.navigate(Rutas.CREAR_COMANDA)
                            }
                        },

                        onLongClick = {
                            //Click largo -> borrar (solo si es camarero)
                            if (isCamarero) {
                                comandaAEliminar = comanda
                            }
                        }
                    )
                }
            }
        }
    }
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
    onClick:() -> Unit,
    onLongClick: () -> Unit
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
            ){
                // Cabecera
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

                // Productos
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

                // Total
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

                // Ubicación
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

            if (!esServida) {
                // Comanda no Servida
                Button(
                    onClick = onServirClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar como servida")
                }
            } else {
                // Comanda Servida"
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