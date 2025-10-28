package com.thermal.monitoring

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.presentation.auth.LoginFragment
import com.thermal.monitoring.presentation.dashboard.DashboardOperadorFragment
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

        if (savedInstanceState == null) {
            verificarSesion()
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
            .replace(R.id.fragment_container, LoginFragment())
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
                // TODO: Implementar dashboard de admin
                Toast.makeText(
                    this,
                    "Dashboard de Admin - Próximamente",
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