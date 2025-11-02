package com.thermal.monitoring

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "thermal_monitoring_channel"
        private const val CHANNEL_NAME = "Eventos de Thermal Monitoring"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Titulo: ${it.title}")
            Log.d(TAG, "Cuerpo: ${it.body}")
            mostrarNotificacion(it.title, it.body)
        }

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
            val titulo = remoteMessage.data["title"] ?: "Nuevo Evento"
            val mensaje = remoteMessage.data["body"] ?: "Se ha detectado un nuevo evento"
            mostrarNotificacion(titulo, mensaje)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")



        // TODO: enviar el token al servidor

    }

    private fun mostrarNotificacion(titulo: String?, mensaje: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de eventos detectados"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titulo ?: "Thermal Monitoring")
            .setContentText(mensaje ?: "Nueva notificacion")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}