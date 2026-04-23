package com.apktados.ruleta.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apktados.ruleta.MainActivity
import com.apktados.ruleta.R

object NotificationHelper {

    private const val CHANNEL_ID = "victorias_channel"
    private const val CHANNEL_NAME = "Victorias"
    private const val CHANNEL_DESCRIPTION = "Notificaciones de victoria en la ruleta"

    const val EXTRA_MOSTRAR_TIEMPO = "mostrar_tiempo"
    const val EXTRA_TIEMPO_MS = "tiempo_ms"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun mostrarVictoria(context: Context, tiempoResolucionMs: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_MOSTRAR_TIEMPO, true)
            putExtra(EXTRA_TIEMPO_MS, tiempoResolucionMs)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Has ganado!")
            .setContentText("Pulsa para ver el tiempo de resolución")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }
    }
}