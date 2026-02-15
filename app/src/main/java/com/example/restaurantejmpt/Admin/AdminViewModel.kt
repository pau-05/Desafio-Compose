package com.example.restaurantejmpt.Admin

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.restaurantejmpt.Colecciones
import com.example.restaurantejmpt.Model.Rol
import com.example.restaurantejmpt.Model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.text.get

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "Admin"

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Lista observable de usuarios
    private val _usuarios = mutableStateListOf<Usuario>()
    val usuarios: List<Usuario> = _usuarios

    // -------------------------------
    // Registrar un nuevo usuario con UID como documentId
    // -------------------------------
    fun registrarUsuario(email: String, contrasenia: String, roles: List<Rol>) {
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
                        roles = rolesFirestore.map { Rol.valueOf(it) }
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
                    "roles" to usuario.roles.map { it.name }
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
}
