package com.example.restaurantejmpt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Login.LoginScreen
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Rutas.Rutas
import com.example.restaurantejmpt.ui.theme.RestauranteJMPTTheme
import kotlin.getValue
import com.example.restaurantejmpt.Admin.*
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Model.Usuario

class MainActivity : ComponentActivity() {
    //Instancias de ViewModels compartidas
    private val adminViewModel: AdminViewModel by viewModels()
    private val personaViewModel: PersonaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestauranteJMPTTheme {
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
                    composable(Rutas.UPDATE + "/{uid}") { backStackEntry ->
                        //Recupero la uid del usuario a modificar y se la mando como un parametro más
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        ModificarUsuario(uid, adminViewModel)
                    }
                    composable(Rutas.INSERT) {
                        FormularioUsuario(adminViewModel)
                    }
                }
            }
        }
    }
}