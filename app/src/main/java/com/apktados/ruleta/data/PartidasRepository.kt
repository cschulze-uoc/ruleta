package com.apktados.ruleta.data

import android.content.Context

class PartidasRepository(context: Context) {

    private val dbHelper = RuletaDbHelper(context.applicationContext)
    private val dao = PartidasDao(dbHelper)

    fun guardarPartida(jugador: String, monedasFinales: Int, lat: Double? = null, lon: Double? = null): Long {
        return dao.insertarPartida(
            fecha = System.currentTimeMillis(),
            jugador = jugador,
            monedasFinales = monedasFinales,
            latitud = lat,
            longitud = lon
        )
    }

    fun top10(): List<Partida> = dao.obtenerTop10PorMonedas()

    fun historial(): List<Partida> = dao.obtenerTodasPorFechaDesc()

    fun close() = dbHelper.close()
}