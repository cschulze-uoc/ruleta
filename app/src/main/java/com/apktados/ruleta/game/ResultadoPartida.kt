package com.apktados.ruleta.game

sealed interface ResultadoPartida {
    val monedasFinales: Int

    data class Victoria(
        override val monedasFinales: Int,
        val tiempoResolucionMs: Long
    ) : ResultadoPartida

    data class Derrota(
        override val monedasFinales: Int
    ) : ResultadoPartida
}
