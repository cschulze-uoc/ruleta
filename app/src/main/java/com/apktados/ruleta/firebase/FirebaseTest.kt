package com.apktados.ruleta.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object FirebaseTest {

    fun escribirPrueba() {
        val database = FirebaseDatabase.getInstance("https://ruletauoc-default-rtdb.europe-west1.firebasedatabase.app/")
        val ref = database.getReference("prueba")

        ref.setValue("Hola desde la app")
            .addOnSuccessListener {
                Log.d("FIREBASE_TEST", "Dato guardado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_TEST", "Error al guardar", e)
            }
    }
}