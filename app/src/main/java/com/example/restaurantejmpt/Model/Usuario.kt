package com.example.restaurantejmpt.Model

class Usuario (val id: String = "",
                val email: String = "",
                val contrasenia: String = "",
                val roles: List<String> = emptyList()
                )