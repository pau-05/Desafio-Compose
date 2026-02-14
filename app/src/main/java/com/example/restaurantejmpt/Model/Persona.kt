package com.example.restaurantejmpt.Model

data class Persona(
    val email: String = "",
    val contraseña: String = "",
    val rol: ArrayList<String> = ArrayList<String>()
)
