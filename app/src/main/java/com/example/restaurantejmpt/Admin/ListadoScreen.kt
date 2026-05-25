package com.example.restaurantejmpt.Admin

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.restaurantejmpt.Model.Usuario
import com.example.restaurantejmpt.Rutas.Rutas

@Composable
fun Listado(viewModel: AdminViewModel = AdminViewModel(), navController: NavController){
    val usuarios = viewModel.usuarios //Observa la lista desde el ViewModel
    val error by viewModel.errorMessage.collectAsState()
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsuarios()
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = "Usuarios Registrados",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        //Muestra mensaje de error o lista vacía
        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else if (usuarios.isEmpty()) {
            Text(
                text = "Lista vacía",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            ) {
                items(usuarios) { usuario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {
                                    viewModel.seleccionarUsuario(usuario) // Guardamos el objeto entero
                                    navController.navigate(Rutas.UPDATE)
                                },

                                onLongClick = {
                                    //Click largo -> borrar
                                    usuarioAEliminar = usuario
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Email: ${usuario.email}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Roles: ${
                                        usuario.roles.joinToString { it }
                                    }",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    usuarioAEliminar?.let { usuario ->

        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Eliminar a ${usuario.email}?") },

            confirmButton = {
                Button(onClick = {
                    viewModel.borrarUsuario(usuario.id)
                    usuarioAEliminar = null
                }) {
                    Text("Sí")
                }
            },

            dismissButton = {
                Button(onClick = {
                    usuarioAEliminar = null
                }) {
                    Text("No")
                }
            }
        )
    }
}