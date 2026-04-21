package com.apktados.ruleta.notification

public class TimeUtil {
    fun formatearTiempo(ms: Long): String {
        val totalSegundos = ms / 1000
        val minutos = totalSegundos / 60
        val segundos = totalSegundos % 60
        val milisegundos = ms % 1000

        return String.format("%02d:%02d.%03d", minutos, segundos, milisegundos)
    }
}