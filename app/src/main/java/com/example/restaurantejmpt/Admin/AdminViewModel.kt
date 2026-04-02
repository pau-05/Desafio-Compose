package com.example.restaurantejmpt.Admin

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.restaurantejmpt.Colecciones
import com.example.restaurantejmpt.Model.Producto
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.text.get

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "Admin"

    // Estado para el usuario que se está editando
    private val _usuarioSeleccionado = MutableStateFlow<Usuario?>(null)
    val usuarioSeleccionado: StateFlow<Usuario?> = _usuarioSeleccionado

    fun seleccionarUsuario(usuario: Usuario) {
        _usuarioSeleccionado.value = usuario
    }

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Lista observable de usuarios
    private val _usuarios = mutableStateListOf<Usuario>()
    val usuarios: List<Usuario> = _usuarios

    // -------------------------------
    // Registrar un nuevo usuario con UID como documentId
    // -------------------------------
    fun registrarUsuario(email: String, contrasenia: String, roles: List<String>) {
        isLoading.value = true
        errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, contrasenia)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        val nuevoUsuario = hashMapOf(
                            "email" to email,
                            "contrasenia" to contrasenia,
                            "roles" to roles
                        )

                        db.collection(Colecciones.USERS)
                            .document(uid)
                            .set(nuevoUsuario)
                            .addOnSuccessListener {
                                Log.d(TAG, "Usuario $email guardado correctamente")
                                loadUsuarios() // refresca lista
                            }
                            .addOnFailureListener {
                                errorMessage.value = it.message
                            }
                    }
                } else {
                    errorMessage.value = task.exception?.message
                }
            }
    }

    // -------------------------------
    // CRUD Firestore para Admin
    // -------------------------------

    // Cargar todos los usuarios
    fun loadUsuarios() {
        isLoading.value = true
        db.collection(Colecciones.USERS)
            .get()
            .addOnSuccessListener { snapshot ->
                _usuarios.clear()
                for (doc in snapshot.documents) {
                    val rolesFirestore = doc.get("roles") as? List<String> ?: emptyList()
                    val usuario = Usuario(
                        id = doc.id, // el UID
                        email = doc.getString("email") ?: "",
                        contrasenia = doc.getString("contrasenia") ?: "",
                        roles = rolesFirestore
                    )
                    _usuarios.add(usuario)
                }
                isLoading.value = false
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                isLoading.value = false
            }
    }

    //Actualizar un usuario
    fun actualizarUsuario(usuario: Usuario) {
        isLoading.value = true
        db.collection(Colecciones.USERS)
            .document(usuario.id)
            .update(
                mapOf(
                    "email" to usuario.email,
                    "contrasenia" to usuario.contrasenia,
                    "roles" to usuario.roles
                )
            )
            .addOnSuccessListener {
                Log.d(TAG, "Usuario ${usuario.email} actualizado")
                loadUsuarios()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                isLoading.value = false
            }
    }

    // Borrar un usuario
    fun borrarUsuario(uid: String) {
        isLoading.value = true
        db.collection(Colecciones.USERS)
            .document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Usuario $uid eliminado")
                loadUsuarios()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                isLoading.value = false
            }
    }

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
        isLoading.value = true
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

                isLoading.value = false
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                isLoading.value = false
            }
    }

    // Agregar producto
    fun agregarProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        tipo: String,
        imagenUrl: String = ""
    ) {
        isLoading.value = true
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
                isLoading.value = false
            }
    }

    // Actualizar producto
    fun actualizarProducto(producto: Producto) {
        isLoading.value = true

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
                isLoading.value = false
            }
    }

    // Borrar producto
    fun borrarProducto(productoId: String) {
        isLoading.value = true

        db.collection(Colecciones.PRODUCTOS)
            .document(productoId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Producto $productoId eliminado")
                loadProductos()
            }
            .addOnFailureListener {
                errorMessage.value = it.message
                isLoading.value = false
            }
    }
}
