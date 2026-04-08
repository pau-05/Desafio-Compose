package com.example.restaurantejmpt.Camarero

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.restaurantejmpt.Colecciones
import com.example.restaurantejmpt.Model.Comanda
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ComandaViewModel: ViewModel()  {
    val TAG = "ComandaVM"

    /*
           El problema era que Firebase no podía determinar automáticamente a qué instancia de
           Realtime Database conectarse. Al especificar la URL explícitamente, se resolvió el
           problema.
        */
    private val databaseReference = FirebaseDatabase
        .getInstance("https://restaurantejmpt-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference(Colecciones.COMANDAS)

    private val _comandas = MutableStateFlow<List<Comanda>>(emptyList())
    val comandas: StateFlow<List<Comanda>> get() = _comandas

    init {
        observeComandas()
    }

    private fun observeComandas() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevosMens = snapshot.children.mapNotNull { it.getValue(Comanda::class.java) }.sortedByDescending { it.fechaHora }
                _comandas.value = nuevosMens.toList()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al escuchar los cambios en la base de datos", error.toException())
            }
        })
    }

    fun sendComanda(comanda: Comanda) {
        val newMessageId = databaseReference.push().key
        if (newMessageId != null) {
            val comandaConId = comanda.copy(id = newMessageId)
            databaseReference.child(newMessageId).setValue(comandaConId)
                .addOnSuccessListener {
                    Log.d(TAG, "Comanda guardada correctamente con ID: $newMessageId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al guardar comanda", e)
                }
        } else {
            Log.e(TAG, "No se pudo generar una clave única")
        }
    }
}