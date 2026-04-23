package com.apktados.ruleta.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class RuletaDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PARTIDAS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_PARTIDAS)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Ruleta.db"

        private const val SQL_CREATE_PARTIDAS =
            "CREATE TABLE ${RuletaContract.Partidas.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${RuletaContract.Partidas.COLUMN_FECHA} INTEGER NOT NULL," +
                    "${RuletaContract.Partidas.COLUMN_JUGADOR} TEXT NOT NULL," +
                    "${RuletaContract.Partidas.COLUMN_MONEDAS_FINALES} INTEGER NOT NULL," +
                    "${RuletaContract.Partidas.COLUMN_LATITUD} REAL," +
                    "${RuletaContract.Partidas.COLUMN_LONGITUD} REAL," +
                    "${RuletaContract.Partidas.COLUMN_TIEMPO_RESOLUCION} INTEGER NOT NULL" +
                    ")"

        private const val SQL_DELETE_PARTIDAS =
            "DROP TABLE IF EXISTS ${RuletaContract.Partidas.TABLE_NAME}"
    }
}