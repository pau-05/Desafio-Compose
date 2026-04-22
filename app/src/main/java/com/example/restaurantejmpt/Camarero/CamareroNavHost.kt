package com.example.restaurantejmpt.Camarero

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.restaurantejmpt.Rutas.Rutas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Components.TopDropdownMenu
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Productos.ProductoViewModel

private const val TAG = "CamareroNavHost"

@Composable
fun CamareroNavHost(
    modifier: Modifier = Modifier,
    camareroId: String,
    productoViewModel: ProductoViewModel,
    comandaViewModel: ComandaViewModel,
    personaViewModel: PersonaViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val ubicacionViewModel: MapsViewModel = viewModel()

    Scaffold (
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopDropdownMenu(
                personaViewModel,
                onLogout = onLogout
            )
        }
    ){ paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = Rutas.CREAR_COMANDA,
                modifier = modifier
            ) {
                //Log.d(TAG, "Dentro de NavHost, definiendo rutas")
                composable(Rutas.CREAR_COMANDA) {
                    CrearComandaScreen(
                        onNavigateToMapa = {
                            navController.navigate(Rutas.SELECCIONAR_UBICACION)
                        },
                        ubicacionViewModel = ubicacionViewModel,
                        camareroId = camareroId,
                        productoViewModel = productoViewModel,
                        comandaViewModel = comandaViewModel
                    )
                }

                composable(Rutas.SELECCIONAR_UBICACION) {
                    MapaSeleccionScreen(
                        ubicacionViewModel = ubicacionViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}