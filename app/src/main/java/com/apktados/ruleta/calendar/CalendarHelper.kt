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
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.VISIBLE
            )

            val cursor = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                "${CalendarContract.Calendars.VISIBLE} = 1",
                null,
                null
            )

            var calendarId: Long? = null

            cursor?.use {

                val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                val accessIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

                var googleCalendarId: Long? = null
                var fallbackCalendarId: Long? = null

                while (it.moveToNext()) {

                    val accessLevel = it.getInt(accessIndex)
                    val id = it.getLong(idIndex)

                    if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {

                        // Guardamos primero Google si existe
                        val accountTypeIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE)
                        val accountType = it.getString(accountTypeIndex)

                        if (accountType == "com.google") {
                            googleCalendarId = id
                            break
                        }

                        // Si no hay Google, usamos cualquiera editable
                        if (fallbackCalendarId == null) {
                            fallbackCalendarId = id
                        }
                    }
                }

                calendarId = googleCalendarId ?: fallbackCalendarId
            }

            if (calendarId == null) {
                throw IllegalStateException("No hay calendarios editables disponibles")
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, "Victoria en APKtados")
                put(CalendarContract.Events.DESCRIPTION, "$jugador se retiró con $monedasFinales monedas")
                put(CalendarContract.Events.DTSTART, fechaMillis)
                put(CalendarContract.Events.DTEND, fechaMillis + 60 * 60 * 1000)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 0)
            }

            val uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)

            if (uri == null) {
                throw IllegalStateException("No se pudo insertar el evento")
            }
        }
    }
}