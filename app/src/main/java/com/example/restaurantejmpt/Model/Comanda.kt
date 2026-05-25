package com.example.restaurantejmpt.Model

data class Comanda(
    val id: String = "",
    val servido: Boolean = false,
    val fechaHora: Long = System.currentTimeMillis(),
    val ubicacionLat: Double = 0.0,
    val ubicacionLng: Double = 0.0,
    val productos: List<ProductoComanda> = emptyList(),
    val total: Double = 0.0,
    val camareroId: String = "" //Para saber qué camarero creó la comanda
)