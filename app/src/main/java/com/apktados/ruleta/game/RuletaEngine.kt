package com.apktados.ruleta.game

data class ResultadoRuleta(
    val numero: Int,
    val esRojo: Boolean,
    val esPar: Boolean,
    val esPasse: Boolean
)

class RuletaEngine {

    fun girar(): ResultadoRuleta {

        val numero = (0..36).random()

        val esRojo = numero in numerosRojos
        val esPar = numero != 0 && numero % 2 == 0
        val esPasse = numero in 19..36

        return ResultadoRuleta(
            numero = numero,
            esRojo = esRojo,
            esPar = esPar,
            esPasse = esPasse
        )
    }

    companion object {
        private val numerosRojos = setOf(
            1,3,5,7,9,
            12,14,16,18,
            19,21,23,25,27,
            30,32,34,36
        )
    }
}