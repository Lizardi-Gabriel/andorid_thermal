package com.thermal.monitoring.presentation.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.R
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.databinding.FragmentMiPerfilBinding
import com.thermal.monitoring.presentation.admin.DashboardAdminFragment
import com.thermal.monitoring.presentation.admin.GenerarReporteFragment
import com.thermal.monitoring.presentation.admin.GestionUsuariosFragment
import com.thermal.monitoring.presentation.dashboard.DashboardOperadorFragment
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MiPerfilFragment : Fragment() {

    private var _binding: FragmentMiPerfilBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: TokenManager
    private val viewModel: MiPerfilViewModel by viewModels()
    private var rolUsuario: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawer()
        setupObservers()
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE

            val usuarioId = tokenManager.obtenerUserId().first()
            val username = tokenManager.obtenerUsername().first()
            val correo = tokenManager.obtenerCorreo().first()
            val rol = tokenManager.obtenerRol().first()

            rolUsuario = rol

            // Datos en la tarjeta central con foto
            binding.tvNombreUsuarioCentral.text = username ?: "Usuario"
            binding.chipRol.text = rol ?: "N/A"

            // Datos en la tarjeta de informacion
            binding.tvUsuarioId.text = usuarioId?.toString() ?: "N/A"
            binding.tvNombreUsuario.text = username ?: "N/A"
            binding.tvCorreo.text = correo ?: "N/A"

            // Cambiar color del chip segun rol
            if (rol == RolUsuarioEnum.ADMIN.name) {
                binding.chipRol.setChipBackgroundColorResource(R.color.gris)
                binding.navigationView.menu.clear()
                binding.navigationView.inflateMenu(R.menu.drawer_menu_admin)
            } else {
                binding.chipRol.setChipBackgroundColorResource(R.color.button_primary)
                binding.navigationView.menu.clear()
                binding.navigationView.inflateMenu(R.menu.drawer_menu)

                // Mostrar estadisticas para operador
                binding.cardEstadisticas.visibility = View.VISIBLE

                // Cargar estadisticas desde el backend
                if (usuarioId != null) {
                    viewModel.cargarEstadisticasUsuario(usuarioId)
                }
            }

            // Cargar datos en el header del drawer
            val headerView = binding.navigationView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.tvNombreUsuario).text = username ?: "Usuario"
            headerView.findViewById<TextView>(R.id.tvRolUsuario).text = rol ?: "Rol"

            // Marcar item activo
            binding.navigationView.setCheckedItem(R.id.nav_perfil)

            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.estadisticasState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Ya hay un progressBar global
                }
                is Resource.Success -> {
                    val estadisticas = resource.data
                    if (estadisticas != null) {
                        binding.tvTotalGestionados.text = estadisticas.totalEventosGestionados.toString()
                        binding.tvConfirmados.text = estadisticas.eventosConfirmados.toString()
                        binding.tvDescartados.text = estadisticas.eventosDescartados.toString()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar estadisticas: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Mostrar valores en 0 si hay error
                    binding.tvTotalGestionados.text = "0"
                    binding.tvConfirmados.text = "0"
                    binding.tvDescartados.text = "0"
                }
            }
        }
    }

    private fun setupDrawer() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // Opciones de Admin
                R.id.nav_dashboard -> {
                    navegarADashboard()
                }
                R.id.nav_gestion_usuarios -> {
                    navegarAGestionUsuarios()
                }
                R.id.nav_generar_reporte -> {
                    navegarAGenerarReporte()
                }
                // Opciones de Operador
                R.id.nav_pendientes -> {
                    navegarADashboard()
                }
                R.id.nav_historial -> {
                    navegarADashboard()
                }
                // Opciones comunes
                R.id.nav_perfil -> {
                    // Ya estamos aqui
                }
                R.id.nav_logout -> {
                    cerrarSesion()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun navegarADashboard() {
        if (rolUsuario == RolUsuarioEnum.ADMIN.name) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardAdminFragment())
                .commit()
        } else {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardOperadorFragment())
                .commit()
        }
    }

    private fun navegarAGestionUsuarios() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GestionUsuariosFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navegarAGenerarReporte() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GenerarReporteFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            tokenManager.limpiarDatos()
        }
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}