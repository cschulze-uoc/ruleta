package com.apktados.ruleta.data

import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns

class PartidasDao(private val dbHelper: RuletaDbHelper) {

    fun insertarPartida(
        fecha: Long,
        jugador: String,
        monedasFinales: Int,
        latitud: Double? = null,
        longitud: Double? = null,
        tiempoResolucionMs: Long
    ): Long {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(RuletaContract.Partidas.COLUMN_FECHA, fecha)
            put(RuletaContract.Partidas.COLUMN_JUGADOR, jugador)
            put(RuletaContract.Partidas.COLUMN_MONEDAS_FINALES, monedasFinales)
            if (latitud != null) put(RuletaContract.Partidas.COLUMN_LATITUD, latitud)
            if (longitud != null) put(RuletaContract.Partidas.COLUMN_LONGITUD, longitud)
            put(RuletaContract.Partidas.COLUMN_TIEMPO_RESOLUCION, tiempoResolucionMs)
        }

        // Devuelve id de la fila o -1 si error
        return db.insert(RuletaContract.Partidas.TABLE_NAME, null, values)
    }

    fun obtenerTop10PorMonedas(): List<Partida> {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            RuletaContract.Partidas.COLUMN_FECHA,
            RuletaContract.Partidas.COLUMN_JUGADOR,
            RuletaContract.Partidas.COLUMN_MONEDAS_FINALES,
            RuletaContract.Partidas.COLUMN_LATITUD,
            RuletaContract.Partidas.COLUMN_LONGITUD,
            RuletaContract.Partidas.COLUMN_TIEMPO_RESOLUCION,
        )

        val sortOrder = "${RuletaContract.Partidas.COLUMN_MONEDAS_FINALES} DESC"

        val cursor = db.query(
            RuletaContract.Partidas.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder,
            "10"
        )

        return cursor.use { c -> c.toPartidas() }
    }

    fun obtenerTodasPorFechaDesc(): List<Partida> {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            RuletaContract.Partidas.COLUMN_FECHA,
            RuletaContract.Partidas.COLUMN_JUGADOR,
            RuletaContract.Partidas.COLUMN_MONEDAS_FINALES,
            RuletaContract.Partidas.COLUMN_LATITUD,
            RuletaContract.Partidas.COLUMN_LONGITUD,
            RuletaContract.Partidas.COLUMN_TIEMPO_RESOLUCION
        )

        val sortOrder = "${RuletaContract.Partidas.COLUMN_FECHA} DESC"

        val cursor = db.query(
            RuletaContract.Partidas.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )

        return cursor.use { c -> c.toPartidas() }
    }

    private fun Cursor.toPartidas(): List<Partida> {
        val res = ArrayList<Partida>(count)

        val idxId = getColumnIndexOrThrow(BaseColumns._ID)
        val idxFecha = getColumnIndexOrThrow(RuletaContract.Partidas.COLUMN_FECHA)
        val idxJugador = getColumnIndexOrThrow(RuletaContract.Partidas.COLUMN_JUGADOR)
        val idxMonedas = getColumnIndexOrThrow(RuletaContract.Partidas.COLUMN_MONEDAS_FINALES)
        val idxLat = getColumnIndexOrThrow(RuletaContract.Partidas.COLUMN_LATITUD)
        val idxLon = getColumnIndexOrThrow(RuletaContract.Partidas.COLUMN_LONGITUD)
        val idxTiempo = getColumnIndexOrThrow(RuletaContract.Partidas.COLUMN_TIEMPO_RESOLUCION)

        while (moveToNext()) {
            res.add(
                Partida(
                    id = getLong(idxId),
                    fecha = getLong(idxFecha),
                    jugador = getString(idxJugador),
                    monedasFinales = getInt(idxMonedas),
                    latitud = if (isNull(idxLat)) null else getDouble(idxLat),
                    longitud = if (isNull(idxLon)) null else getDouble(idxLon),
                    tiempoResolucionMs = getLong(idxTiempo)
                )
            )
        }
        return res
    }
}