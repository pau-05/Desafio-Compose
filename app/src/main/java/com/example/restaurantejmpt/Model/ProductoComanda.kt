package com.example.restaurantejmpt.Model

data class ProductoComanda(
    val productoId: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0
)