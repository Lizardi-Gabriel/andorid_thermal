package com.thermal.monitoring

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.presentation.auth.BienvenidaFragment
import com.thermal.monitoring.presentation.dashboard.DashboardOperadorFragment
import com.thermal.monitoring.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.solicitarPermisoNotificaciones(this) { token ->
            // Token obtenido, se enviara al backend despues del login
            Log.d("MainActivity", "Token FCM obtenido: $token")
        }

        if (savedInstanceState == null) {
            verificarSesion()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        NotificationHelper.manejarResultadoPermiso(requestCode, grantResults) { token ->
            Log.d("MainActivity", "Token FCM obtenido: $token")
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
                Toast.makeText(
                    this,
                    "Dashboard de Admin - Proximamente",
                    Toast.LENGTH_LONG
                ).show()
                mostrarLogin()
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
}