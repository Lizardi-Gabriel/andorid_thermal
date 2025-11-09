package com.thermal.monitoring.presentation.admin

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
import com.google.android.material.datepicker.MaterialDatePicker
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.R
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.databinding.FragmentGenerarReporteBinding
import com.thermal.monitoring.presentation.perfil.MiPerfilFragment
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GenerarReporteFragment : Fragment() {

    private var _binding: FragmentGenerarReporteBinding? = null
    private val binding get() = _binding!!

    private val reportesViewModel: ReportesViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    private var fechaInicioSeleccionada: String? = null
    private var fechaFinSeleccionada: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerarReporteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawer()
        setupListeners()
        setupObservers()
        cargarDatosUsuario()

        binding.navigationView.setCheckedItem(R.id.nav_generar_reporte)
    }

    private fun setupDrawer() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    navegarADashboard()
                }
                R.id.nav_gestion_usuarios -> {
                    navegarAGestionUsuarios()
                }
                R.id.nav_generar_reporte -> {

                }
                R.id.nav_perfil -> {
                    navegarAMiPerfil()
                }
                R.id.nav_logout -> {
                    cerrarSesion()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun navegarAMiPerfil() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MiPerfilFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val username = tokenManager.obtenerUsername().first()
            val rol = tokenManager.obtenerRol().first()

            val headerView = binding.navigationView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.tvNombreUsuario).text = username ?: "Administrador"
            headerView.findViewById<TextView>(R.id.tvRolUsuario).text = rol ?: "Admin"
        }
    }

    private fun setupListeners() {
        binding.btnSeleccionarRango.setOnClickListener {
            mostrarRangeDatePicker()
        }

        binding.btnGenerar.setOnClickListener {
            generarReporte()
        }
    }

    private fun setupObservers() {
        reportesViewModel.reportePDFState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEstado.visibility = View.VISIBLE
                    binding.tvEstado.text = "Generando reporte..."
                    binding.btnGenerar.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEstado.visibility = View.VISIBLE
                    binding.tvEstado.text = "Reporte generado exitosamente"
                    binding.btnGenerar.isEnabled = true

                    resource.data?.let { responseBody ->
                        guardarYAbrirPDF(responseBody)
                    }

                    reportesViewModel.limpiarEstado()

                    // Ocultar mensaje despues de 3 segundos
                    binding.tvEstado.postDelayed({
                        if (isAdded) {
                            binding.tvEstado.visibility = View.GONE
                        }
                    }, 3000)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEstado.visibility = View.VISIBLE
                    binding.tvEstado.text = "Error: ${resource.message}"
                    binding.btnGenerar.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al generar reporte",
                        Toast.LENGTH_LONG
                    ).show()

                    reportesViewModel.limpiarEstado()
                }
                null -> { /* Estado inicial */ }
            }
        }
    }

    private fun mostrarRangeDatePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Seleccionar rango de fechas")

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            val fechaInicio = Date(selection.first)
            val fechaFin = Date(selection.second)

            val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatoApi.timeZone = TimeZone.getTimeZone("UTC")

            val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            fechaInicioSeleccionada = formatoApi.format(fechaInicio)
            fechaFinSeleccionada = formatoApi.format(fechaFin)

            binding.tvRangoSeleccionado.text =
                "Rango: ${formatoMostrar.format(fechaInicio)} - ${formatoMostrar.format(fechaFin)}"
        }

        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun generarReporte() {
        reportesViewModel.generarReportePDF(fechaInicioSeleccionada, fechaFinSeleccionada)
    }

    private fun guardarYAbrirPDF(responseBody: okhttp3.ResponseBody) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "reporte_thermal_$timestamp.pdf"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = requireContext().contentResolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        responseBody.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    Toast.makeText(
                        requireContext(),
                        "Reporte guardado en Descargas: $fileName",
                        Toast.LENGTH_LONG
                    ).show()

                    abrirPDFMediaStore(uri)
                }
            } else {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val file = java.io.File(downloadsDir, fileName)

                responseBody.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(
                    requireContext(),
                    "Reporte guardado en Descargas: $fileName",
                    Toast.LENGTH_LONG
                ).show()

                abrirPDF(file)
            }

        } catch (e: Exception) {
            android.util.Log.e("GenerarReporte", "Error al guardar PDF", e)
            Toast.makeText(
                requireContext(),
                "Error al guardar PDF: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun abrirPDFMediaStore(uri: android.net.Uri) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Ignorar silenciosamente
        }
    }

    private fun abrirPDF(file: java.io.File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Ignorar silenciosamente
        }
    }

    private fun navegarADashboard() {
        parentFragmentManager.popBackStack()
    }

    private fun navegarAGestionUsuarios() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GestionUsuariosFragment())
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