package com.example.restaurantejmpt

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Login.LoginScreen
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Rutas.Rutas
import com.example.restaurantejmpt.ui.theme.RestauranteJMPTTheme
import kotlin.getValue
import com.example.restaurantejmpt.Admin.*
import com.example.restaurantejmpt.Camarero.CamareroNavHost
import com.example.restaurantejmpt.Camarero.ComandaViewModel
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Productos.ProductoViewModel

class MainActivity : ComponentActivity() {

    private val personaViewModel: PersonaViewModel by viewModels()
    private val productoViewModel: ProductoViewModel by viewModels()
    private val comandaViewModel: ComandaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestauranteJMPTTheme {
                val navController = rememberNavController()
                val paddingValues = remember { PaddingValues(0.dp) }

                AppNavHost(
                    navController = navController,
                    padding = paddingValues,
                    startRoute = Rutas.LOGIN,
                    personaViewModel = personaViewModel,
                    productoViewModel = productoViewModel,
                    comandaViewModel = comandaViewModel
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavHost(navController: NavHostController, padding: PaddingValues,
               startRoute: String, personaViewModel: PersonaViewModel,
               productoViewModel: ProductoViewModel,
               comandaViewModel: ComandaViewModel) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = Modifier.padding(padding),

    ) {
        // -------------------
        // LOGIN
        // -------------------
        composable(Rutas.LOGIN) {
            LoginScreen(
                loginViewModel = personaViewModel,
                onLoginSuccess = { rolSeleccionado ->
                    when (rolSeleccionado) {
                        Rol.ADMIN -> {
                            navController.navigate(Rutas.ADMIN) {
                                popUpTo(Rutas.LOGIN) { inclusive = true }
                            }
                        }

                        Rol.CAMARERO -> {
                            navController.navigate(Rutas.CAMAMERO) {
                                popUpTo(Rutas.LOGIN) { inclusive = true }
                            }
                        }

                        Rol.COCINERO -> TODO()
                        Rol.CLIENTE -> TODO()
                        Rol.BARMAN -> TODO()
                    }
                }
            )
        }
        // -------------------
        // ADMIN
        // -------------------
        composable(Rutas.ADMIN) {
            AdminNavHost(productoViewModel = productoViewModel, personaViewModel= personaViewModel)
        }

        // -------------------
        // CAMARERO
        // -------------------
        composable(Rutas.CAMAMERO) {
            val usuario = personaViewModel.getCurrentUser()
            if (usuario!= null) CamareroNavHost(camareroId = usuario.uid,
                productoViewModel = productoViewModel,
                comandaViewModel = comandaViewModel, personaViewModel = personaViewModel
            )
        }
    }
}