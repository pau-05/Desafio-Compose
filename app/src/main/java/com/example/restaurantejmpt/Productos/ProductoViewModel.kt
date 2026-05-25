package com.example.restaurantejmpt.Productos

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantejmpt.Colecciones
import com.example.restaurantejmpt.Model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductoViewModel: ViewModel()  {
    private val db = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // Estado para indicar si se está subiendo
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading
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

    // Actualizar producto CON nueva imagen (subir imagen a Storage)
    fun actualizarProductoConImagen(
        productoId: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        tipo: String,
        nuevaImagenUri: Uri,
        imagenActualUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Subir la nueva imagen a Storage
                val imageName = "productos/${UUID.randomUUID()}.jpg"
                val imageRef = storageRef.child(imageName)

                Log.d(TAG, "Subiendo nueva imagen: $imageName")
                imageRef.putFile(nuevaImagenUri).await()

                // 2. Obtener URL de descarga
                val downloadUrl = imageRef.downloadUrl.await()
                val nuevaImagenUrl = downloadUrl.toString()

                Log.d(TAG, "Nueva imagen subida. URL: $nuevaImagenUrl")

                // 3. Eliminar imagen antigua (opcional, para no acumular)
                if (imagenActualUrl.isNotEmpty() && imagenActualUrl.startsWith("https://firebasestorage")) {
                    try {
                        // Eliminar imagen antigua si no es la misma
                        val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagenActualUrl)
                        oldImageRef.delete().await()
                        Log.d(TAG, "Imagen antigua eliminada")
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo eliminar imagen antigua: ${e.message}")
                    }
                }

                // 4. Actualizar producto en Firestore con la nueva URL
                db.collection(Colecciones.PRODUCTOS)
                    .document(productoId)
                    .update(
                        mapOf(
                            "nombre" to nombre,
                            "descripcion" to descripcion,
                            "precio" to precio,
                            "tipo" to tipo,
                            "imagenUrl" to nuevaImagenUrl
                        )
                    )
                    .await()

                _isLoading.value = false
                loadProductos()
                onSuccess()

            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Error al actualizar producto con imagen", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    //Actualizar imagen sin foto
    fun actualizarProductoSinImagen(
        productoId: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        tipo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updates = mapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "precio" to precio,
            "tipo" to tipo
        )

        db.collection(Colecciones.PRODUCTOS)
            .document(productoId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Producto actualizado sin imagen")
                loadProductos()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al actualizar producto", e)
                onError(e.message ?: "Error desconocido")
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

    // Función para subir imagen y obtener URL
    fun subirImagenYGuardarProducto(
        imageUri: Uri,
        nombre: String,
        descripcion: String,
        precio: Double,
        tipo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true

            try {
                // Crear nombre único para la imagen
                val imageName = "productos/${UUID.randomUUID()}.jpg"
                val imageRef = storageRef.child(imageName)

                // Subir la imagen
                val uploadTask = imageRef.putFile(imageUri).await()

                // Obtener la URL de descarga (HTTPS)
                val downloadUrl = imageRef.downloadUrl.await()
                val urlString = downloadUrl.toString()

                // Guardar el producto con la URL de Firebase Storage
                agregarProducto(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    tipo = tipo,
                    imagenUrl = urlString  // ✅ URL HTTPS válida
                )

                _isUploading.value = false
                onSuccess()

            } catch (e: Exception) {
                _isUploading.value = false
                onError(e.message ?: "Error al subir imagen")
            }
        }
    }
}