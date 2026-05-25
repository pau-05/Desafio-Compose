package com.example.restaurantejmpt.Login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Model.Rol

@Composable
fun LoginScreen(
    loginViewModel: PersonaViewModel,
    onLoginSuccess: (Rol) -> Unit
) {
    val context = LocalContext.current
    // Observamos el estado del ViewModel
    val isLoading by loginViewModel.isLoading.collectAsState()
    val loginSuccess by loginViewModel.loginSuccess.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()
    val roles by loginViewModel.userRoles.collectAsState()
    var showRoleDialog by remember { mutableStateOf(false) }
    // Estados locales para el formulario
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var isRegistering by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Registrar Cuenta" else "Iniciar Sesión",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de Email
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de Contraseña
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Principal (Login o Registro)
        Button(
            onClick = {
                if (isRegistering) {
                    loginViewModel.registrarPersona(email.text, password.text)
                } else {
                    loginViewModel.logearPersona(email.text, password.text)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.text.isNotEmpty() && password.text.isNotEmpty()
        ) {
            Text(if (isRegistering) "Registrar" else "Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch entre Login y Registro
        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(
                text = if (isRegistering) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate"
            )
        }

        // Indicador de carga
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        // Mensaje de error
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        // Navegación al tener éxito
        LaunchedEffect(loginSuccess) {
            if (loginSuccess) {

                val mensaje = if (isRegistering) {
                    "¡Registro completado con éxito!"
                } else {
                    "¡Inicio de sesión correcto!"
                }

                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                showRoleDialog = true
            }
        }
        if (showRoleDialog && roles.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Selecciona tu rol") },
                text = {
                    Column {
                        roles.forEach { rol ->
                            Button(
                                onClick = {
                                    showRoleDialog = false
                                    onLoginSuccess(rol) //  aquí navegamos
                                    loginViewModel.setCurrentRole(rol) //seteamos como se ha inciado sesión
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(rol.name)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}