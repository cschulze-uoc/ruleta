package com.apktados.ruleta.firebase

import com.google.firebase.database.FirebaseDatabase

object FirebaseRepository {

    private const val DATABASE_URL =
        "https://ruletauoc-default-rtdb.europe-west1.firebasedatabase.app/"

    private val database = FirebaseDatabase.getInstance(DATABASE_URL)

    fun guardarPuntuacion(
        nombre: String,
        monedas: Int,
        onOk: () -> Unit,
        onError: (String) -> Unit
    ) {
        val nuevaPuntuacion = OnlineScore(
            nombre = nombre,
            monedas = monedas,
            fecha = System.currentTimeMillis()
        )

        val ref = database.getReference("scores").push()

        ref.setValue(nuevaPuntuacion)
            .addOnSuccessListener {
                onOk()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido al guardar puntuación")
            }
    }

    fun obtenerTop10(
        onOk: (List<OnlineScore>) -> Unit,
        onError: (String) -> Unit
    ) {
        val ref = database.getReference("scores")

        ref.get()
            .addOnSuccessListener { snapshot ->
                val lista = mutableListOf<OnlineScore>()

                for (hijo in snapshot.children) {
                    val score = hijo.getValue(OnlineScore::class.java)
                    if (score != null) {
                        lista.add(score)
                    }
                }

                val top10 = lista
                    .sortedByDescending { it.monedas }
                    .take(10)

                onOk(top10)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error desconocido al leer top 10")
            }
    }
}