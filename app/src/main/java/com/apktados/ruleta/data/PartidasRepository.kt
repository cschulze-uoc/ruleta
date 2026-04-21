package com.apktados.ruleta.data

import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class PartidasRepository(context: Context) {

    private val dbHelper = RuletaDbHelper(context.applicationContext)
    private val dao = PartidasDao(dbHelper)

    fun guardarPartida(
        jugador: String,
        monedasFinales: Int,
        lat: Double? = null,
        lon: Double? = null
    ): Completable {
        return Completable.fromAction {

            dao.insertarPartida(
                fecha = System.currentTimeMillis(),
                jugador = jugador,
                monedasFinales = monedasFinales,
                latitud = lat,
                longitud = lon
            )
        }
    }

    fun top10(): Single<List<Partida>> {
        return Single.fromCallable {
            dao.obtenerTop10PorMonedas()
        }
    }

    fun historial(): Single<List<Partida>> {
        return Single.fromCallable {
            dao.obtenerTodasPorFechaDesc()
        }
    }

    fun close(): Completable {
        return Completable.fromAction {
            dbHelper.close()
        }
    }
}