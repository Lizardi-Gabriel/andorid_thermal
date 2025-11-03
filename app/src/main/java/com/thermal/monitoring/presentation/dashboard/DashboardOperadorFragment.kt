package com.thermal.monitoring.presentation.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.R
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.databinding.FragmentDashboardOperadorBinding
import com.thermal.monitoring.presentation.eventos.DetalleEventoFragment
import com.thermal.monitoring.presentation.eventos.EventoAdapter
import com.thermal.monitoring.presentation.eventos.EventoAdapterOptimizado
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import java.util.TimeZone


@AndroidEntryPoint
class DashboardOperadorFragment : Fragment() {

    private var _binding: FragmentDashboardOperadorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardOperadorViewModel by viewModels()
    private lateinit var eventoAdapter: EventoAdapterOptimizado


    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardOperadorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawer()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        cargarDatosHeaderDrawer()
    }

    private fun setupDrawer() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    viewModel.cargarTodos()
                }
                R.id.nav_pendientes -> {
                    viewModel.cargarEventosPendientes()
                }
                R.id.nav_historial -> {
                    viewModel.cargarMiHistorial()
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


    private fun cargarDatosHeaderDrawer() {
        lifecycleScope.launch {
            val username = tokenManager.obtenerUsername().first()
            val rol = tokenManager.obtenerRol().first()

            val headerView = binding.navigationView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.tvNombreUsuario).text = username ?: "Usuario"
            headerView.findViewById<TextView>(R.id.tvRolUsuario).text = rol ?: "Operador"
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
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.recargarEventos()
        }

        binding.btnSeleccionarFecha.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnSeleccionarFecha.setOnLongClickListener {
            viewModel.limpiarFiltroFecha()
            Toast.makeText(requireContext(), "Filtro de fecha limpiado", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun setupObservers() {
        viewModel.eventosState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutVacio.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false

                    val eventos = resource.data ?: emptyList()

                    binding.tvTituloRecientes.text = "Eventos (${eventos.size})"

                    if (eventos.isEmpty()) {
                        binding.layoutVacio.visibility = View.VISIBLE
                        binding.rvEventos.visibility = View.GONE
                    } else {
                        binding.layoutVacio.visibility = View.GONE
                        binding.rvEventos.visibility = View.VISIBLE
                        eventoAdapter.submitList(eventos)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al cargar eventos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.fechaSeleccionada.observe(viewLifecycleOwner) { fechaApi ->
            actualizarUiFiltros()
        }
        viewModel.filtroActual.observe(viewLifecycleOwner) { filtro ->
            actualizarUiFiltros()
        }
    }

    private fun actualizarUiFiltros() {
        if (!isAdded) return

        val filtro = viewModel.filtroActual.value
        val fechaApi = viewModel.fechaSeleccionada.value

        if (fechaApi != null) {
            val fechaMostrar = parsearFecha(fechaApi, "dd/MM/yyyy")
            binding.btnSeleccionarFecha.text = fechaMostrar
        } else {
            binding.btnSeleccionarFecha.text = "Seleccionar Fecha"
        }

        val tituloBase = when (filtro) {
            DashboardOperadorViewModel.FiltroEvento.TODOS -> "Galería de Eventos"
            DashboardOperadorViewModel.FiltroEvento.PENDIENTES -> "Eventos Pendientes"
            DashboardOperadorViewModel.FiltroEvento.MI_HISTORIAL -> "Mi Historial"
            else -> "Galería de Eventos"
        }

        val fechaTitulo = if (fechaApi != null) "(${parsearFecha(fechaApi, "dd/MM")})" else ""
        binding.toolbar.title = "$tituloBase $fechaTitulo".trim()
    }

    private fun parsearFecha(fechaApi: String, formatoSalida: String): String {
        return try {
            val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoMostrar = SimpleDateFormat(formatoSalida, Locale.getDefault())
            formatoApi.timeZone = TimeZone.getTimeZone("UTC")
            formatoMostrar.timeZone = TimeZone.getTimeZone("UTC")
            val fechaDate = formatoApi.parse(fechaApi)
            formatoMostrar.format(fechaDate!!)
        } catch (e: Exception) {
            fechaApi
        }
    }


    private fun mostrarDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleccionar fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatoApi.timeZone = TimeZone.getTimeZone("UTC")
            val fechaApi = formatoApi.format(Date(selection))

            viewModel.cargarEventosPorFecha(fechaApi)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun navegarADetalle(eventoId: Int) {
        val fragment = DetalleEventoFragment.newInstance(eventoId)
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
        lifecycleScope.launch {
            val userId = tokenManager.obtenerUserId().first()
            if (userId != null) {
                viewModel.setUsuarioId(userId)
            } else {
                // TODO: Manejar caso donde el ID no está
            }
            viewModel.recargarEventos()
        }
    }
}

