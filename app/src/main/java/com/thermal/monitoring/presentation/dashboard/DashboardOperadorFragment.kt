package com.thermal.monitoring.presentation.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.databinding.FragmentDashboardOperadorBinding
import com.thermal.monitoring.presentation.eventos.DetalleEventoFragment
import com.thermal.monitoring.presentation.eventos.EventoAdapter
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DashboardOperadorFragment : Fragment() {

    private var _binding: FragmentDashboardOperadorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardOperadorViewModel by viewModels()
    private lateinit var eventoAdapter: EventoAdapter

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

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        eventoAdapter = EventoAdapter { evento ->
            navegarADetalle(evento.eventoId)
        }

        binding.rvEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventoAdapter
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.cargarEventos()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnSeleccionarFecha.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnEventosPendientes.setOnClickListener {
            Toast.makeText(requireContext(), "Filtrando pendientes...", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistorial.setOnClickListener {
            Toast.makeText(requireContext(), "Ver mi historial...", Toast.LENGTH_SHORT).show()
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

                    binding.tvTituloRecientes.text = "Eventos recientes (${eventos.size})"

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
    }

    private fun mostrarDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleccionar fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val fecha = Date(selection)
            val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val fechaApi = formatoApi.format(fecha)
            val fechaMostrar = formatoMostrar.format(fecha)

            binding.btnSeleccionarFecha.text = fechaMostrar
            viewModel.cargarEventosPorFecha(fechaApi)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun navegarADetalle(eventoId: Int) {
        val fragment = DetalleEventoFragment.newInstance(eventoId)
        parentFragmentManager.beginTransaction()
            .replace(com.thermal.monitoring.R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}