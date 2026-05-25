package com.example.restaurantejmpt.Barman

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.restaurantejmpt.Camarero.ComandaViewModel
import com.example.restaurantejmpt.Model.PersonaViewModel
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarmanMainScreen(
    comandaViewModel: ComandaViewModel,
    personaViewModel: PersonaViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var pantallaActual by remember { mutableStateOf(PantallaBarman.EN_PROCESO) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Menú",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                Divider()

                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close() // Cerramos el menú antes de salir
                            personaViewModel.signOut(context) // Limpia el estado
                            onLogout() // Ejecuta la navegación
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Barman") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = { pantallaActual = PantallaBarman.EN_PROCESO }) {
                        Text("En proceso")
                    }
                    Button(onClick = { pantallaActual = PantallaBarman.SERVIDAS }) {
                        Text("Servidas")
                    }
                }
            }
        ) { paddingValues ->

            Box(modifier = Modifier.padding(paddingValues)) {
                when (pantallaActual) {
                    PantallaBarman.EN_PROCESO -> {
                        ListadoComandasScreen(comandaViewModel,
                            personaViewModel = personaViewModel,
                            navController = navController
                        )
                    }
                    PantallaBarman.SERVIDAS -> {
                        ComandasServidasScreen(
                            comandaViewModel,
                            personaViewModel = personaViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}