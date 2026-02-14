package com.example.restaurantejmpt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Login.LoginScreen
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Rutas.Rutas
import com.example.restaurantejmpt.ui.theme.RestauranteJMPTTheme

class MainActivity : ComponentActivity() {

    // Instancia del ViewModel compartida
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
                }
            }
        }
    }
}