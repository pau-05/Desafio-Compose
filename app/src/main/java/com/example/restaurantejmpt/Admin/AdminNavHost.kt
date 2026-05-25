package com.example.restaurantejmpt.Admin

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Components.TopDropdownMenu
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Productos.FormularioProducto
import com.example.restaurantejmpt.Productos.ListadoProductos
import com.example.restaurantejmpt.Productos.ModificarProducto
import com.example.restaurantejmpt.Productos.ProductoViewModel
import com.example.restaurantejmpt.Rutas.Rutas
@Composable
fun AdminNavHost(
    productoViewModel: ProductoViewModel,
    personaViewModel: PersonaViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val adminViewModel: AdminViewModel = viewModel()

    Scaffold(
        bottomBar = {
            BottomMenuBar(
                currentRoute = navController.currentBackStackEntry?.destination?.route,
                navController = navController
            )
        },
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopDropdownMenu(personaViewModel = personaViewModel, onLogout = onLogout)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.LISTA, // Empezamos en la lista por defecto
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.INSERT) {
                FormularioUsuario(adminViewModel)
            }
            composable(Rutas.UPDATE) { backStackEntry ->
                ModificarUsuario(adminViewModel)
            }
            composable(Rutas.LISTA) {
                Listado(adminViewModel, navController)
            }
            // PRODUCTOS
            composable(Rutas.LISTA_PRODUCTOS) {
                ListadoProductos(productoViewModel, navController, personaViewModel = personaViewModel)
            }
            composable(Rutas.INSERT_PRODUCTO) {
                FormularioProducto(productoViewModel)
            }
            composable(Rutas.UPDATE_PRODUCTO) {
                ModificarProducto(productoViewModel)
            }
        }
    }
}

@Composable
fun BottomMenuBar(
    currentRoute: String?,
    navController: NavHostController
) {
    NavigationBar {
        // --- SECCIÓN USUARIOS ---
        NavigationBarItem(
            selected = currentRoute == Rutas.LISTA,
            onClick = { navController.navigate(Rutas.LISTA) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Usuarios") }
        )

        NavigationBarItem(
            selected = currentRoute == Rutas.INSERT,
            onClick = { navController.navigate(Rutas.INSERT) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Add, null) },
            label = { Text("Añadir Usuario") }
        )

        // --- SECCIÓN PRODUCTOS ---
        NavigationBarItem(
            selected = currentRoute == Rutas.LISTA_PRODUCTOS,
            onClick = { navController.navigate(Rutas.LISTA_PRODUCTOS) { launchSingleTop = true } },
            icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
            label = { Text("Catálogo") }
        )

        NavigationBarItem(
            selected = currentRoute == Rutas.INSERT_PRODUCTO,
            onClick = { navController.navigate(Rutas.INSERT_PRODUCTO) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Add, null) },
            label = { Text("Añadir Producto") }
        )
    }
}