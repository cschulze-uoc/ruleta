package com.apktados.ruleta.data

data class Partida(
    val id: Long,
    val fecha: Long,
    val jugador: String,
    val monedasFinales: Int,
    val latitud: Double?,
    val longitud: Double?,
    val tiempoResolucionMs: Long
)