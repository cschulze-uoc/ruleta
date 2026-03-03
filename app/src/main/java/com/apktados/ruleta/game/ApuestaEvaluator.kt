package com.apktados.ruleta.game

object ApuestaEvaluator {

    fun esGanadora(tipo: TipoApuesta, resultado: ResultadoRuleta): Boolean {

        return when (tipo) {
            TipoApuesta.ROJO -> resultado.esRojo
            TipoApuesta.NEGRO -> !resultado.esRojo && resultado.numero != 0
            TipoApuesta.PAR -> resultado.esPar
            TipoApuesta.IMPAR -> resultado.numero % 2 != 0
            TipoApuesta.PASSE -> resultado.esPasse
            TipoApuesta.MANQUE -> resultado.numero in 1..18
        }
    }
}