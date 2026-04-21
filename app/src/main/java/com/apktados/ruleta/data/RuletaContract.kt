package com.apktados.ruleta.data

import android.provider.BaseColumns

object RuletaContract {

    object Partidas : BaseColumns {
        const val TABLE_NAME = "partidas"

        const val COLUMN_FECHA = "fecha"
        const val COLUMN_JUGADOR = "jugador"
        const val COLUMN_MONEDAS_FINALES = "monedas_finales"
        const val COLUMN_LATITUD = "latitud"
        const val COLUMN_LONGITUD = "longitud"
        const val COLUMN_TIEMPO_RESOLUCION = "tiempo"
    }
}