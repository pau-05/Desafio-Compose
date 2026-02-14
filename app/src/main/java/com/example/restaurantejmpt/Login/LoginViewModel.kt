package com.example.restaurantejmpt.Login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ==========================
    // ESTADOS
    // ==========================
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole


    // ==========================
    // LOGIN
    // ==========================
    fun loginUsuario(email: String, password: String) {

        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Campos obligatorios"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    obtenerRolUsuario()
                } else {
                    _errorMessage.value = task.exception?.message
                    _isLoading.value = false
                }
            }
    }


    // ==========================
    // OBTENER ROL DESDE FIRESTORE
    // ==========================
    private fun obtenerRolUsuario() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val rol = document.getString("rol")

                _userRole.value = rol
                _loginSuccess.value = true
                _isLoading.value = false
            }
            .addOnFailureListener {
                _errorMessage.value = it.message
                _isLoading.value = false
            }
    }


    fun logout() {
        auth.signOut()
        _loginSuccess.value = false
        _userRole.value = null
    }
}
