package com.example.restaurantejmpt.Admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Model.Usuario


@Composable
fun FormularioUsuario(viewModel: AdminViewModel = AdminViewModel()) {
    var email by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }
    val rolesSeleccionados = remember { mutableStateListOf<String>() }
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
        Rol.entries.forEach { rol ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rolesSeleccionados.contains(rol.name),
                    onCheckedChange = { checked ->
                        if (checked) {
                            rolesSeleccionados.add(rol.name)
                        } else {
                            rolesSeleccionados.remove(rol.name)
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
                roles = rolesSeleccionados
            )
        }) {
            Text("Registrar")
        }
    }
}