package com.apktados.ruleta.calendar

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import io.reactivex.rxjava3.core.Completable
import java.util.TimeZone

class CalendarHelper(private val context: Context) {

    fun guardarVictoria(
        jugador: String,
        monedasFinales: Int,
        fechaMillis: Long
    ): Completable {
        return Completable.fromAction {
            val resolver = context.contentResolver

            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
            )

            val cursor = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )

            var calendarId: Long? = null

            cursor?.use {
                val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                val accountTypeIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE)
                val accessLevelIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

                while (it.moveToNext()) {
                    val accountType = it.getString(accountTypeIndex)
                    val accessLevel = it.getInt(accessLevelIndex)

                    if (
                        accountType == "com.google" &&
                        accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR
                    ) {
                        calendarId = it.getLong(idIndex)
                        break
                    }
                }
            }

            if (calendarId == null) {
                throw IllegalStateException("No se encontró ningún calendario de Google disponible")
            }

            val inicio = fechaMillis
            val fin = fechaMillis + 60 * 60 * 1000

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, "Victoria en APKtados")
                put(
                    CalendarContract.Events.DESCRIPTION,
                    "$jugador se retiró con $monedasFinales monedas"
                )
                put(CalendarContract.Events.DTSTART, inicio)
                put(CalendarContract.Events.DTEND, fin)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            resolver.insert(CalendarContract.Events.CONTENT_URI, values)
                ?: throw IllegalStateException("No se pudo insertar el evento")
        }
    }
}