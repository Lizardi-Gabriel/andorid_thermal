package com.thermal.monitoring.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private const val NOTIFICATION_PERMISSION_CODE = 1001

    fun solicitarPermisoNotificaciones(activity: Activity, onTokenObtenido: (String) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            } else {
                Log.d(TAG, "Permiso de notificaciones ya concedido")
                obtenerTokenFCM(onTokenObtenido)
            }
        } else {
            obtenerTokenFCM(onTokenObtenido)
        }
    }

    fun obtenerTokenFCM(onTokenObtenido: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Error al obtener token FCM", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "Token FCM: $token")

            // Callback con el token
            onTokenObtenido(token)
        }
    }

    fun manejarResultadoPermiso(requestCode: Int, grantResults: IntArray, onTokenObtenido: (String) -> Unit) {
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso de notificaciones concedido")
                obtenerTokenFCM(onTokenObtenido)
            } else {
                Log.d(TAG, "Permiso de notificaciones denegado")
            }
        }
    }


}