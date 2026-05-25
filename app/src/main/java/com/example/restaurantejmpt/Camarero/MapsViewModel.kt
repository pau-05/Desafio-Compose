package com.example.restaurantejmpt.Camarero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {
    private val _ubicacionSeleccionada = MutableStateFlow<LatLng?>(null)
    val ubicacionSeleccionada: StateFlow<LatLng?> = _ubicacionSeleccionada

    val home = LatLng(38.693245786259595, -4.108508457997148)

    private val _cameraPosition = MutableStateFlow(CameraPosition.fromLatLngZoom(home, 17f)) // Posición inicial con zoom 17.
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition



    fun actualizarUbicacion(latLng: LatLng) {
        viewModelScope.launch {
            _ubicacionSeleccionada.value = latLng
        }
    }

    fun limpiarUbicacion() {
        viewModelScope.launch {
            _ubicacionSeleccionada.value = null
        }
    }
}