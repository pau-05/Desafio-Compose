package com.example.restaurantejmpt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
<<<<<<< HEAD
import androidx.compose.foundation.border
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
=======
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Login.LoginScreen
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Rutas.Rutas
>>>>>>> feature/Login
import com.example.restaurantejmpt.ui.theme.RestauranteJMPTTheme
import kotlin.getValue
import com.example.restaurantejmpt.Admin.*
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Model.Usuario

class MainActivity : ComponentActivity() {
<<<<<<< HEAD
    private val adminViewModel: AdminViewModel by viewModels()
=======

    // Instancia del ViewModel compartida
    private val personaViewModel: PersonaViewModel by viewModels()

>>>>>>> feature/Login
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestauranteJMPTTheme {
<<<<<<< HEAD
                //Listado(adminViewModel)
                //FormularioUsuario(adminViewModel)
            }
        }
    }
}

@Composable
fun Listado(viewModel: AdminViewModel = AdminViewModel()){
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
                            .padding(vertical = 4.dp),
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
                                        usuario.roles.joinToString { it.name }
                                    }",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Button(onClick = {
                                    usuarioAEliminar = usuario
                                }) {
                                    Text("Borrar")
                                }
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

@Composable
fun FormularioUsuario(viewModel: AdminViewModel = AdminViewModel()){
    var email by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }
    val rolesSeleccionados = remember { mutableStateListOf<Rol>() }
    var emailError by remember { mutableStateOf(false) }
    var contraError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium
        )

        //Textfield para el email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = it.isBlank()
            },
            label = { Text("Email") },
            isError = emailError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (emailError) {
            Text(
                text = "El email no puede estar vacío",
                color = MaterialTheme.colorScheme.error
            )
        }

        //Textfield para la contraseña
        OutlinedTextField(
            value = contrasenia,
            onValueChange = {
                contrasenia = it
                contraError = it.length < 6
            },
            label = { Text("Contraseña") },
            isError = contraError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (contraError) {
            Text(
                text = "Mínimo 6 caracteres",
                color = MaterialTheme.colorScheme.error
            )
        }

        Text("Roles")

        Rol.values().forEach { rol ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rolesSeleccionados.contains(rol),
                    onCheckedChange = { checked ->
                        if (checked) {
                            rolesSeleccionados.add(rol)
                        } else {
                            rolesSeleccionados.remove(rol)
                        }
                    }
                )
                Text(rol.name)
            }
        }

        Button(onClick = {
            if (rolesSeleccionados.isEmpty()) {
                //Validación en caso de que se queden vacíos los roles
                return@Button
            }

            viewModel.registrarUsuario(
                email = email,
                contrasenia = contrasenia,
                roles = rolesSeleccionados.toList()
            )
        }) {
            Text("Registrar")
        }
    }
=======
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Rutas.LOGIN
                ) {
                    composable(Rutas.LOGIN) {
                        LoginScreen(
                            loginViewModel = personaViewModel,
                            onLoginSuccess = {
                                // Aquí usamos la lógica: si el ViewModel dice que fue exitoso,
                                // mostramos el Toast.
                                // el mensaje depende de qué botón pulsó el usuario.

                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    "Operación realizada con éxito",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                    composable(Rutas.REGISTER) {

                    }
                }
            }
        }
    }
>>>>>>> feature/Login
}