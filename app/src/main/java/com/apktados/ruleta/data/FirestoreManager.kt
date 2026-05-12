package com.apktados.ruleta.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    fun guardarPartida(
        jugador: String,
        puntuacion: Int
    ) {

        val partida = hashMapOf(
            "jugador" to jugador,
            "puntuacion" to puntuacion,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("partidas")
            .add(partida)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Partida guardada")
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error", e)
            }
    }
}