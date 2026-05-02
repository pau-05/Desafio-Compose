package com.example.restaurantejmpt.Barman

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.restaurantejmpt.Camarero.ComandaViewModel
import com.example.restaurantejmpt.Model.Comanda
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Rutas.Rutas

@Composable
fun ComandasServidasScreen(
    comandaViewModel: ComandaViewModel,
    personaViewModel: PersonaViewModel,
    navController: NavController
) {
    //Obtiene el rol con el que se ha iniciado sesión
    val rol by personaViewModel.currentRole.collectAsState()

    //Booleano que nos indicará si tendrá los permisos de edición o no
    val isCamarero = rol == Rol.CAMARERO

    val todasLasComandas by comandaViewModel.comandas.collectAsState()
    val comandasServidas = todasLasComandas.filter { it.servido }

    val totalRecaudado = comandasServidas.sumOf { it.total }

    var comandaAEliminar by remember { mutableStateOf<Comanda?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Total recaudado: $totalRecaudado €",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (comandasServidas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay comandas servidas")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(comandasServidas) { comanda ->
                    ComandaCard(
                        comanda = comanda,
                        onServirClick = {},
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