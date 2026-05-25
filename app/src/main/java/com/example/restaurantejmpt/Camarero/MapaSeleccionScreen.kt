package com.example.restaurantejmpt.Camarero

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapaSeleccionScreen(
    ubicacionViewModel: MapsViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    //Estado para el marcador temporal (ubicación seleccionada)
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    //Estado de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = ubicacionViewModel.cameraPosition.value
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = true
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
    }

    // Solicitar permiso y obtener ubicación actual
    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    currentLocation = loc
                    loc?.let {
                        val newPos = LatLng(it.latitude, it.longitude)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(newPos, 15f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona ubicación de la mesa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedLocation?.let {
                                ubicacionViewModel.actualizarUbicacion(it)
                                navController.popBackStack()
                            } ?: Toast.makeText(context, "Selecciona un punto en el mapa", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true
                ),
                onMapLongClick = { latLng ->
                    selectedLocation = latLng
                    Toast.makeText(context, "Ubicación seleccionada: ${latLng.latitude}, ${latLng.longitude}", Toast.LENGTH_SHORT).show()
                },
                onPOIClick = { poi ->
                    selectedLocation = poi.latLng
                    Toast.makeText(context, "POI seleccionado: ${poi.name}", Toast.LENGTH_SHORT).show()
                },
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    Toast.makeText(context, "Ubicación seleccionada", Toast.LENGTH_SHORT).show()
                },
                onMyLocationButtonClick = {
                    currentLocation?.let {
                        val loc = LatLng(it.latitude, it.longitude)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(loc, 15f)
                            )
                        }
                    }
                    true
                }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Mesa",
                        snippet = "Toca para cambiar"
                    )
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Toca en el mapa o mantén pulsado para seleccionar la mesa",
                    modifier = Modifier.padding(8.dp),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }
        }
    }
}