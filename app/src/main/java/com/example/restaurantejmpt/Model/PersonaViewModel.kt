package com.example.restaurantejmpt.Model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.text.isNotEmpty

class PersonaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "Jose Maria"

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    private val _userRoles = MutableStateFlow<List<Rol>>(emptyList())
    val userRoles: StateFlow<List<Rol>> = _userRoles
    // Lista observable de personas
    private val _personas = mutableStateListOf<Usuario>()
    val personas: List<Usuario> = _personas
    val loginSuccess = MutableStateFlow(false)
    val isGoogleLogin = MutableStateFlow(false)

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun registrarPersona(email: String, contraseña: String) {
        isLoading.value = true
        errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    val rolesIniciales = arrayListOf("CLIENTE")
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        val nuevaPersona = hashMapOf(
                            "email" to email,
                            "contraseña" to contraseña,
                            "roles" to rolesIniciales
                        )

                    //Creacion de la coleccion de usuarios
                    db.collection("usuarios") // Esta es la colección que busca tu función obtenerTodosLosUsuarios
                        .document(uid)
                        .set(nuevaPersona)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Usuario con rol CLIENTE guardado correctamente")
                            isGoogleLogin.value = false
                            loginSuccess.value = true
                            isLoading.value = false
                        }
                    }
                } else {
                    isLoading.value = false
                    errorMessage.value = task.exception?.message
                }
            }

    }
    //Funcion de logear
    fun logearPersona(email: String, contraseña: String) {
        isLoading.value = true
        errorMessage.value = null

        auth.signInWithEmailAndPassword(email, contraseña)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    obtenerRolesUsuario()
                } else {
                    errorMessage.value = task.exception?.message
                }
            }
    }

    fun signOut(context: Context) {
            // 1. Cierra sesión en Firebase (esto funciona para cualquier método)
            auth.signOut()

            // 2. Reiniciar estados del ViewModel (sin lógica de Google)
            loginSuccess.value = false
            errorMessage.value = null
            isLoading.value = false
            _userRoles.value = emptyList() // ¡Importante limpiar esto!
            isGoogleLogin.value = false

            Log.d(TAG, "Sesión cerrada correctamente")
    }
    fun obtenerRolesUsuario() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val rolesString = document.get("roles") as? List<*> ?: emptyList<Any>()

                    val rolesConvertidos = rolesString.mapNotNull { nombre ->
                        Rol.values().find { it.name == nombre.toString() }
                    }

                    _userRoles.value = rolesConvertidos
                    loginSuccess.value = true
                }
            }
            .addOnFailureListener {
                errorMessage.value = "Error obteniendo roles"
            }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}