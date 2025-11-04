package com.thermal.monitoring

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.presentation.admin.DashboardAdminFragment
import com.thermal.monitoring.presentation.auth.BienvenidaFragment
import com.thermal.monitoring.presentation.dashboard.DashboardOperadorFragment
import com.thermal.monitoring.presentation.eventos.DetalleEventoFragment
import com.thermal.monitoring.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    private var eventoIdDesdeNotificacion: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.solicitarPermisoNotificaciones(this) { token ->
            android.util.Log.d("MainActivity", "Token FCM obtenido: $token")
        }

        // Verificar si viene de una notificacion
        manejarIntentNotificacion(intent)

        if (savedInstanceState == null) {
            verificarSesion()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        manejarIntentNotificacion(intent)
    }

    private fun manejarIntentNotificacion(intent: android.content.Intent?) {
        intent?.let {
            android.util.Log.d("MainActivity", "Intent recibido: $it")

            var eventoId: Int? = null

            try {
                val intValue = it.getIntExtra(MyFirebaseMessagingService.EXTRA_EVENTO_ID, -1)
                if (intValue != -1) {
                    eventoId = intValue
                }
            } catch (e: ClassCastException) {
            }

            if (eventoId == null) {
                try {
                    val stringValue = it.getStringExtra(MyFirebaseMessagingService.EXTRA_EVENTO_ID)
                    eventoId = stringValue?.toIntOrNull()
                } catch (e: ClassCastException) {
                }
            }

            eventoId?.let { id ->
                eventoIdDesdeNotificacion = id
                android.util.Log.d("MainActivity", "EventoId obtenido: $id")
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        NotificationHelper.manejarResultadoPermiso(requestCode, grantResults) { token ->
            android.util.Log.d("MainActivity", "Token FCM obtenido: $token")
        }
    }

    private fun verificarSesion() {
        lifecycleScope.launch {
            val haySesion = tokenManager.haySesionActiva().first()

            if (haySesion) {
                val rol = tokenManager.obtenerRol().first()
                val username = tokenManager.obtenerUsername().first()

                Toast.makeText(
                    this@MainActivity,
                    "Bienvenido $username",
                    Toast.LENGTH_SHORT
                ).show()

                navegarADashboard(rol)

                eventoIdDesdeNotificacion?.let { eventoId ->
                    navegarADetalleEvento(eventoId)
                    eventoIdDesdeNotificacion = null
                }
            } else {
                mostrarLogin()
            }
        }
    }

    private fun mostrarLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BienvenidaFragment())
            .commit()
    }

    private fun navegarADashboard(rol: String?) {
        when (rol) {
            "OPERADOR" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardOperadorFragment())
                    .commit()
            }
            "ADMIN" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardAdminFragment())
                    .commit()

            }
            else -> {
                Toast.makeText(
                    this,
                    "Rol desconocido",
                    Toast.LENGTH_SHORT
                ).show()
                mostrarLogin()
            }
        }
    }

    private fun navegarADetalleEvento(eventoId: Int) {
        supportFragmentManager.executePendingTransactions()

        val fragment = DetalleEventoFragment.newInstance(eventoId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        android.util.Log.d("MainActivity", "Navegando al detalle del evento: $eventoId")
    }
}