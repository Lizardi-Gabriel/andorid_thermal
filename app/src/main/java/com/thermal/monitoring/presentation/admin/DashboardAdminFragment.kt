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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.R
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.databinding.FragmentDashboardAdminBinding
import com.thermal.monitoring.presentation.eventos.DetalleEventoFragment
import com.thermal.monitoring.presentation.eventos.EventoAdapterOptimizado
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DashboardAdminFragment : Fragment() {

    private var _binding: FragmentDashboardAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardAdminViewModel by viewModels()
    private lateinit var eventoAdapter: EventoAdapterOptimizado

    private val reportesViewModel: ReportesViewModel by viewModels()

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawer()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        cargarDatosUsuario()

        // Establecer fecha de hoy en el boton
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.btnFiltrarFechas.text = hoy

        binding.navigationView.setCheckedItem(R.id.nav_dashboard)

    }

    private fun setupDrawer() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    viewModel.limpiarFiltros()
                }
                R.id.nav_gestion_usuarios -> {
                    navegarAGestionUsuarios()
                }
                R.id.nav_generar_reporte -> {
                    navegarAGenerarReporte()
                }
                R.id.nav_perfil -> {
                    Toast.makeText(requireContext(), "Mi Perfil - Proximamente", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> {
                    cerrarSesion()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun navegarAGenerarReporte() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GenerarReporteFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun mostrarDialogoGenerarReporte() {
        val dialogBinding = com.thermal.monitoring.databinding.DialogGenerarReporteBinding.inflate(layoutInflater)

        var fechaInicioSeleccionada: String? = null
        var fechaFinSeleccionada: String? = null

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSeleccionarRango.setOnClickListener {
            mostrarRangeDatePickerParaReporte { fechaInicio, fechaFin ->
                fechaInicioSeleccionada = fechaInicio
                fechaFinSeleccionada = fechaFin

                val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val fechaInicioDate = formatoApi.parse(fechaInicio)
                val fechaFinDate = formatoApi.parse(fechaFin)

                if (fechaInicioDate != null && fechaFinDate != null) {
                    dialogBinding.tvRangoSeleccionado.text =
                        "Rango: ${formatoMostrar.format(fechaInicioDate)} - ${formatoMostrar.format(fechaFinDate)}"
                }
            }
        }

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnGenerar.setOnClickListener {
            dialogBinding.progressBar.visibility = View.VISIBLE
            dialogBinding.btnGenerar.isEnabled = false

            reportesViewModel.generarReportePDF(fechaInicioSeleccionada, fechaFinSeleccionada)
        }

        // Observer para el estado del reporte
        reportesViewModel.reportePDFState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    dialogBinding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    dialogBinding.progressBar.visibility = View.GONE
                    dialog.dismiss()

                    resource.data?.let { responseBody ->
                        guardarYAbrirPDF(responseBody)
                    }

                    reportesViewModel.limpiarEstado()
                }
                is Resource.Error -> {
                    dialogBinding.progressBar.visibility = View.GONE
                    dialogBinding.btnGenerar.isEnabled = true

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

        dialog.show()
    }

    private fun mostrarRangeDatePickerParaReporte(onRangoSeleccionado: (String, String) -> Unit) {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Seleccionar rango de fechas")

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { selection ->
            val fechaInicio = Date(selection.first)
            val fechaFin = Date(selection.second)

            val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatoApi.timeZone = TimeZone.getTimeZone("UTC")

            val fechaInicioStr = formatoApi.format(fechaInicio)
            val fechaFinStr = formatoApi.format(fechaFin)

            onRangoSeleccionado(fechaInicioStr, fechaFinStr)
        }

        picker.show(parentFragmentManager, "DATE_RANGE_PICKER_REPORTE")
    }

    private fun guardarYAbrirPDF(responseBody: okhttp3.ResponseBody) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "reporte_thermal_$timestamp.pdf"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ - Usar MediaStore
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
                // Android 9 y anteriores
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
            android.util.Log.e("DashboardAdmin", "Error al guardar PDF", e)
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
            Toast.makeText(
                requireContext(),
                "No hay aplicacion para abrir PDF. El archivo esta en Descargas.",
                Toast.LENGTH_LONG
            ).show()
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
            Toast.makeText(
                requireContext(),
                "No hay aplicacion para abrir PDF. Busca el archivo en Descargas.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun navegarAGestionUsuarios() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GestionUsuariosFragment())
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

    private fun setupRecyclerView() {
        eventoAdapter = EventoAdapterOptimizado { evento ->
            navegarADetalle(evento.eventoId)
        }

        binding.rvEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventoAdapter
        }
    }

    private fun setupListeners() {
        binding.chipTodos.setOnClickListener {
            viewModel.filtrarPorEstatus(null)
        }

        binding.chipPendientes.setOnClickListener {
            viewModel.filtrarPorEstatus(EstatusEventoEnum.PENDIENTE)
        }

        binding.chipConfirmados.setOnClickListener {
            viewModel.filtrarPorEstatus(EstatusEventoEnum.CONFIRMADO)
        }

        binding.chipDescartados.setOnClickListener {
            viewModel.filtrarPorEstatus(EstatusEventoEnum.DESCARTADO)
        }

        binding.btnFiltrarFechas.setOnClickListener {
            mostrarRangeDatePicker()
        }

        binding.btnLimpiarFiltros.setOnClickListener {
            viewModel.limpiarFiltros()
            binding.chipTodos.isChecked = true
            binding.btnFiltrarFechas.text = "Rango de Fechas"
        }
    }

    private fun setupObservers() {
        viewModel.estadisticasState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Mostrar loading si es necesario
                }
                is Resource.Success -> {
                    resource.data?.let { stats ->
                        binding.tvTotalEventos.text = stats.totalEventos.toString()
                        binding.tvTotalDetecciones.text = stats.totalDetecciones.toString()
                        binding.tvPendientes.text = stats.eventosPendientes.toString()
                        binding.tvConfirmados.text = stats.eventosConfirmados.toString()
                        binding.tvDescartados.text = stats.eventosDescartados.toString()
                        binding.tvPromedioDetecciones.text =
                            "Promedio: ${stats.promedioDeteccionesPorEvento} detecciones/evento"
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar estadisticas: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel.eventosState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvVacio.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE

                    val eventos = resource.data ?: emptyList()
                    binding.tvTituloLista.text = "Historial de Eventos (${eventos.size})"

                    if (eventos.isEmpty()) {
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvEventos.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvEventos.visibility = View.VISIBLE
                        eventoAdapter.submitList(eventos)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar eventos: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

            val formatoMostrar = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            val fechaInicioApi = formatoApi.format(fechaInicio)
            val fechaFinApi = formatoApi.format(fechaFin)

            val fechaInicioMostrar = formatoMostrar.format(fechaInicio)
            val fechaFinMostrar = formatoMostrar.format(fechaFin)

            binding.btnFiltrarFechas.text = "$fechaInicioMostrar - $fechaFinMostrar"
            viewModel.filtrarPorRangoFechas(fechaInicioApi, fechaFinApi)
        }

        picker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun navegarADetalle(eventoId: Int) {
        val fragment = DetalleEventoFragment.newInstance(eventoId, esAdmin = true)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun cerrarSesion() {
        viewModel.logout()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        // Asegurar que siempre haya fecha de hoy si no hay filtros
        viewModel.establecerFechaHoySiNoHayFiltro()

        // Actualizar UI del botón
        viewModel.fechaInicioFiltro.value?.let { fechaInicio ->
            viewModel.fechaFinFiltro.value?.let { fechaFin ->
                if (fechaInicio == fechaFin) {
                    // Es un solo día
                    val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val fecha = formatoApi.parse(fechaInicio)
                        binding.btnFiltrarFechas.text = formatoMostrar.format(fecha!!)
                    } catch (e: Exception) {
                        binding.btnFiltrarFechas.text = fechaInicio
                    }
                } else {
                    // Es un rango
                    val formatoMostrar = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val fechaInicioDate = formatoApi.parse(fechaInicio)
                        val fechaFinDate = formatoApi.parse(fechaFin)
                        binding.btnFiltrarFechas.text =
                            "${formatoMostrar.format(fechaInicioDate!!)} - ${formatoMostrar.format(fechaFinDate!!)}"
                    } catch (e: Exception) {
                        binding.btnFiltrarFechas.text = "$fechaInicio - $fechaFin"
                    }
                }
            }
        }

        binding.navigationView.setCheckedItem(R.id.nav_dashboard)

    }

}