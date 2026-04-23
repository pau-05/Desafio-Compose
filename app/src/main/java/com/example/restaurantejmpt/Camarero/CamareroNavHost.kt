package com.example.restaurantejmpt.Camarero

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.restaurantejmpt.Rutas.Rutas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Barman.ComandasServidasScreen
import com.example.restaurantejmpt.Barman.ListadoComandasScreen
import com.example.restaurantejmpt.Model.PersonaViewModel
import com.example.restaurantejmpt.Productos.ProductoViewModel
import kotlinx.coroutines.launch

private const val TAG = "CamareroNavHost"

@OptIn(ExperimentalMaterial3Api::class)
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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Ruta actual (para selected)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    Text("Menú", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))

                    NavigationDrawerItem(
                        label = { Text("Comandas sin servir") },
                        selected = currentRoute == Rutas.LISTADO_COMANDAS,
                        icon = { Icon(Icons.Default.Archive, null) },
                        onClick = {
                            navController.navigate(Rutas.LISTADO_COMANDAS) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Comandas servidas") },
                        selected = currentRoute == Rutas.COMANDAS_SERVIDAS,
                        icon = { Icon(Icons.Default.ContentPaste, null) },
                        onClick = {
                            navController.navigate(Rutas.COMANDAS_SERVIDAS) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Crear comanda") },
                        selected = currentRoute == Rutas.CREAR_COMANDA,
                        icon = { Icon(Icons.Default.Add, null) },
                        onClick = {
                            navController.navigate(Rutas.CREAR_COMANDA) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        label = { Text("Salir") },
                        selected = currentRoute == Rutas.LOGOUT,
                        icon = { Icon(Icons.Default.Logout, null) },
                        onClick = {
                            scope.launch {
                                drawerState.close() //Cerramos el menú antes de salir
                                personaViewModel.signOut(context) //Limpia el estado
                                onLogout() //Ejecuta la navegación
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            topBar = {
                TopAppBar(
                    title = { Text("Camarero") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(
                    navController = navController,
                    startDestination = Rutas.CREAR_COMANDA,
                    modifier = modifier
                ) {
                    //Crear comanda
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

                    //Ubicación
                    composable(Rutas.SELECCIONAR_UBICACION) {
                        MapaSeleccionScreen(
                            ubicacionViewModel = ubicacionViewModel,
                            navController = navController
                        )
                    }

                    //Lista de comandas sin servir
                    composable(Rutas.LISTADO_COMANDAS) {
                        ListadoComandasScreen(
                            comandaViewModel = comandaViewModel,
                            personaViewModel = personaViewModel,
                            navController = navController
                        )
                    }

                    //Lista de comandas servidas
                    composable(Rutas.COMANDAS_SERVIDAS) {
                        ComandasServidasScreen(
                            comandaViewModel = comandaViewModel,
                            personaViewModel = personaViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}