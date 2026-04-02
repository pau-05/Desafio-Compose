package com.example.restaurantejmpt.Model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val tipo: String = "plato",
    val imagenUrl: String = ""
)