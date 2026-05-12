package com.apktados.ruleta.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.apktados.ruleta.data.Partida

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

    fun obtenerTop10(onResult: (List<Partida>) -> Unit) {

        db.collection("partidas")
            .orderBy("puntuacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->

                val lista = result.documents.mapNotNull { doc ->

                    val jugador = doc.getString("jugador") ?: return@mapNotNull null
                    val puntuacion = doc.getLong("puntuacion")?.toInt() ?: return@mapNotNull null
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    Partida(
                        id = 0,
                        jugador = doc.getString("jugador") ?: "",
                        monedasFinales = (doc.getLong("puntuacion") ?: 0L).toInt(),
                        fecha = 0L,
                        latitud = null,
                        longitud = null,
                        tiempoResolucionMs = 0L
                    )
                }

                onResult(lista)
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error obteniendo top10", e)
                onResult(emptyList())
            }
    }
}