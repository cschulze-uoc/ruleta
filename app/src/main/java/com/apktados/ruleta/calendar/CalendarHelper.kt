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
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
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
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                    if (idIndex != -1) {
                        calendarId = it.getLong(idIndex)
                    }
                }
            }

            if (calendarId == null) {
                throw IllegalStateException("No se encontró ningún calendario disponible")
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