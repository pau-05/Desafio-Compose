package com.example.restaurantejmpt.Productos

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.restaurantejmpt.Colecciones
import com.example.restaurantejmpt.Model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductoViewModel: ViewModel()  {
    private val db = FirebaseFirestore.getInstance()
    private val _isLoading = MutableStateFlow(false)  // ← Estado de loading
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage = MutableStateFlow<String?>(null)
    val TAG = "Producto"

    // =========================================================
    // CRUD PARA LOS PRODUCTOS (PLATOS Y BEBIDAS)
    // =========================================================

    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado

    fun seleccionarProducto(producto: Producto) {
        _productoSeleccionado.value = producto
    }

    private val _productos = mutableStateListOf<Producto>()
    val productos: List<Producto> = _productos

    // Cargar todos los productos
    fun loadProductos() {
        _isLoading.value = true
        db.collection(Colecciones.PRODUCTOS)
            .get()
            .addOnSuccessListener { snapshot ->
                _productos.clear()

                for (doc in snapshot.documents) {
                    val producto = Producto(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        tipo = doc.getString("tipo") ?: "plato",
                        imagenUrl = doc.getString("imagenUrl") ?: ""
                    )
                    _productos.add(producto)
                }

                _isLoading.value = false
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                _isLoading.value = false
            }
    }

    // Agregar producto
    fun agregarProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        tipo: String,
        imagenUrl: String
    ) {
        _isLoading.value = true
        errorMessage.value = null

        val nuevoProducto = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "tipo" to tipo,
            "imagenUrl" to imagenUrl
        )

        db.collection(Colecciones.PRODUCTOS)
            .add(nuevoProducto)
            .addOnSuccessListener {
                Log.d(TAG, "Producto añadido correctamente")
                loadProductos()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                _isLoading.value = false
            }
    }

    // Actualizar producto
    fun actualizarProducto(producto: Producto) {
        _isLoading.value = true

        db.collection(Colecciones.PRODUCTOS)
            .document(producto.id)
            .update(
                mapOf(
                    "nombre" to producto.nombre,
                    "descripcion" to producto.descripcion,
                    "precio" to producto.precio,
                    "tipo" to producto.tipo,
                    "imagenUrl" to producto.imagenUrl
                )
            )
            .addOnSuccessListener {
                Log.d(TAG, "Producto ${producto.nombre} actualizado")
                loadProductos()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                _isLoading.value = false
            }
    }

    // Borrar producto
    fun borrarProducto(productoId: String) {
        _isLoading.value = true

        db.collection(Colecciones.PRODUCTOS)
            .document(productoId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Producto $productoId eliminado")
                loadProductos()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                _isLoading.value = false
            }
    }
}