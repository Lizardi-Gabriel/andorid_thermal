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
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.databinding.FragmentDashboardOperadorBinding
import com.thermal.monitoring.presentation.eventos.EventoAdapter
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

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
            // Click en un evento
            Toast.makeText(
                requireContext(),
                "Evento #${evento.eventoId} - ${evento.estatus}",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Navegar al detalle del evento
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
            // Reiniciar la actividad para volver al login
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnEventosPendientes.setOnClickListener {
            Toast.makeText(requireContext(), "Filtrando pendientes...", Toast.LENGTH_SHORT).show()
            // TODO: Implementar filtro de pendientes
        }

        binding.btnHistorial.setOnClickListener {
            Toast.makeText(requireContext(), "Ver mi historial...", Toast.LENGTH_SHORT).show()
            // TODO: Implementar historial personal
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

        viewModel.eventosPendientes.observe(viewLifecycleOwner) { count ->
            binding.tvContadorPendientes.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}