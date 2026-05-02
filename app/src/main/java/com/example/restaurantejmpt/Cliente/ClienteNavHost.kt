package com.example.restaurantejmpt.Cliente

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Components.TopDropdownMenu
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Productos.ListadoProductos
import com.example.restaurantejmpt.Productos.ProductoViewModel
import com.example.restaurantejmpt.Rutas.Rutas

@Composable
fun ClienteNavHost(
    productoViewModel: ProductoViewModel,
    personaViewModel: PersonaViewModel,
    onLogout: () -> Unit
){
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopDropdownMenu(personaViewModel = personaViewModel, onLogout)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.LISTA_PRODUCTOS, // Empezamos en la lista de productos por defecto
            modifier = Modifier.padding(padding)
        ){
            composable(Rutas.LISTA_PRODUCTOS) {
                ListadoProductos(productoViewModel, navController,
                    personaViewModel = personaViewModel)
            }
        }
    }
}