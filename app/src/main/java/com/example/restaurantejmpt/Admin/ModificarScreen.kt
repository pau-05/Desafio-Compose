package com.example.restaurantejmpt.Admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.restaurantejmpt.Model.Rol

@Composable
fun ModificarUsuario(viewModel: AdminViewModel = AdminViewModel()){
    val context = LocalContext.current

    //Recupero el usuario del listado previo
    val usuario by viewModel.usuarioSeleccionado.collectAsState()

    //En caso de error:
    if (usuario == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Usuario no encontrado")
        }
    } else {
        //Recupero los valores originales para los campos
        var email by remember { mutableStateOf(usuario!!.email) }
        var contrasenia by remember { mutableStateOf(usuario!!.contrasenia) }
        val rolesSeleccionados = remember {
            mutableStateListOf<String>().apply {
                addAll(usuario!!.roles)
            }
        }
        val error by viewModel.errorMessage.collectAsState()

        var emailError by remember { mutableStateOf(false) }
        var contraError by remember { mutableStateOf(false) }

        Column(Modifier.padding(16.dp)) {

            Text(
                text = "Editar usuario",
                style = MaterialTheme.typography.headlineMedium
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

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
                    singleLine = true,
                    readOnly = true
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
                Rol.entries.forEach { rol ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rol.name in rolesSeleccionados,
                            onCheckedChange = { checked ->

                                if (checked)
                                    rolesSeleccionados.add(rol.name)
                                else
                                    rolesSeleccionados.remove(rol.name)
                            }
                        )
                        Text(rol.name)
                    }
                }

                Button(onClick = {
                    viewModel.actualizarUsuario(
                        usuario!!.copy(
                            email = email,
                            contrasenia = contrasenia,
                            roles = rolesSeleccionados
                        )
                    )
                    //En caso de que haya error sale en el Toast
                    if (error.isNullOrEmpty()) {
                        Toast.makeText(context, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}