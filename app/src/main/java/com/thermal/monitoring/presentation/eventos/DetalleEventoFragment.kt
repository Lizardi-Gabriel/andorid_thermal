package com.thermal.monitoring.presentation.eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.databinding.FragmentDetalleEventoBinding
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class DetalleEventoFragment : Fragment() {

    private var _binding: FragmentDetalleEventoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalleEventoViewModel by viewModels()
    private var eventoId: Int = -1
    private var esOperador: Boolean = true

    companion object {
        private const val ARG_EVENTO_ID = "evento_id"

        fun newInstance(eventoId: Int): DetalleEventoFragment {
            return DetalleEventoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_EVENTO_ID, eventoId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventoId = arguments?.getInt(ARG_EVENTO_ID) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleEventoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()

        if (eventoId != -1) {
            viewModel.cargarEvento(eventoId)
        } else {
            Toast.makeText(requireContext(), "Error: ID de evento invalido", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.eventoState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { evento ->
                        mostrarEvento(evento)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al cargar evento",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.actualizarEstatusState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.layoutBotonesAccion.isEnabled = false
                }
                is Resource.Success -> {
                    binding.layoutBotonesAccion.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "Evento actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Error -> {
                    binding.layoutBotonesAccion.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al actualizar evento",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun mostrarEvento(evento: Evento) {
        binding.apply {
            tvEventoId.text = "Evento #${evento.eventoId}"
            tvFecha.text = "Fecha: ${evento.fechaEvento}"

            if (evento.imagenes.isNotEmpty()) {
                val horaInicio = convertirAHoraMexico(evento.imagenes.first().horaSubida)
                val horaFin = convertirAHoraMexico(evento.imagenes.last().horaSubida)
                tvHorario.text = "Horario: $horaInicio - $horaFin"
            } else {
                tvHorario.text = "Horario: Sin horario"
            }

            val totalDetecciones = evento.imagenes.sumOf { it.detecciones.size }
            tvDetecciones.text = "Detecciones: $totalDetecciones"

            configurarEstatus(evento.estatus)

            tvDescripcion.text = evento.descripcion ?: "Sin descripcion disponible"

            if (evento.usuario != null) {
                tvGestionadoPor.visibility = View.VISIBLE
                tvGestionadoPor.text = "Gestionado por: ${evento.usuario.nombreUsuario}"
            }

            if (evento.imagenes.isNotEmpty()) {
                val adapter = ImagenDeteccionAdapter(evento.imagenes)
                viewPagerImagenes.adapter = adapter

                val maxDetecciones = evento.imagenes.maxOfOrNull { it.detecciones.size } ?: 0
                val totalImagenes = evento.imagenes.size

                tvContadorImagenes.text = "Imagen 1 / $totalImagenes | $totalImagenes fotos | Max: $maxDetecciones fumador(es)"

                viewPagerImagenes.registerOnPageChangeCallback(
                    object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            val deteccionesEnEstaImagen = evento.imagenes[position].detecciones.size
                            tvContadorImagenes.text = "Imagen ${position + 1} / $totalImagenes | $totalImagenes fotos | Max: $maxDetecciones | Actual: $deteccionesEnEstaImagen"
                        }
                    }
                )
            } else {
                tvContadorImagenes.text = "Sin imagenes"
            }

            mostrarCalidadAire(evento)

            if (esOperador && evento.estatus == EstatusEventoEnum.PENDIENTE) {
                layoutBotonesAccion.visibility = View.VISIBLE
                setupBotonesAccion(evento)
            } else {
                layoutBotonesAccion.visibility = View.GONE
            }
        }
    }

    private fun configurarEstatus(estatus: EstatusEventoEnum) {
        binding.chipEstatus.apply {
            when (estatus) {
                EstatusEventoEnum.PENDIENTE -> {
                    text = "Pendiente"
                    setChipBackgroundColorResource(R.color.evento_pendiente)
                }
                EstatusEventoEnum.CONFIRMADO -> {
                    text = "Confirmado"
                    setChipBackgroundColorResource(R.color.evento_confirmado)
                }
                EstatusEventoEnum.DESCARTADO -> {
                    text = "Descartado"
                    setChipBackgroundColorResource(R.color.evento_descartado)
                }
            }
        }
    }

    private fun mostrarCalidadAire(evento: Evento) {
        if (evento.registrosCalidadAire.isEmpty()) {
            binding.tvCalidadAire.text = "No hay datos de calidad del aire disponibles"
            return
        }

        val registros = evento.registrosCalidadAire

        val pm10Values = registros.mapNotNull { it.pm10 }
        val pm2p5Values = registros.mapNotNull { it.pm2p5 }
        val pm1p0Values = registros.mapNotNull { it.pm1p0 }

        val promedioPm10 = if (pm10Values.isNotEmpty()) pm10Values.average() else null
        val promedioPm2p5 = if (pm2p5Values.isNotEmpty()) pm2p5Values.average() else null
        val promedioPm1p0 = if (pm1p0Values.isNotEmpty()) pm1p0Values.average() else null

        val texto = buildString {
            append("Promedio de calidad del aire durante este evento:\n\n")

            promedioPm10?.let {
                append("PM 10: %.2f μg/m3\n".format(it))
            }
            promedioPm2p5?.let {
                append("PM 2.5: %.2f μg/m3\n".format(it))
            }
            promedioPm1p0?.let {
                append("PM 1: %.2f μg/m3\n".format(it))
            }

            if (promedioPm10 == null && promedioPm2p5 == null && promedioPm1p0 == null) {
                append("No hay datos suficientes")
            }
        }

        binding.tvCalidadAire.text = texto.trim()
    }

    private fun setupBotonesAccion(evento: Evento) {
        binding.btnConfirmar.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "Confirmar Evento",
                mensaje = "Estas seguro de que quieres confirmar este evento?",
                accion = { viewModel.confirmarEvento(evento.eventoId) }
            )
        }

        binding.btnDescartar.setOnClickListener {
            mostrarDialogoConfirmacion(
                titulo = "Descartar Evento",
                mensaje = "Estas seguro de que quieres descartar este evento?",
                accion = { viewModel.descartarEvento(evento.eventoId) }
            )
        }
    }

    private fun mostrarDialogoConfirmacion(titulo: String, mensaje: String, accion: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Si") { _, _ ->
                accion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun convertirAHoraMexico(timestamp: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")

            val fecha = formatoEntrada.parse(timestamp)

            val formatoSalida = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            formatoSalida.timeZone = TimeZone.getTimeZone("America/Mexico_City")

            formatoSalida.format(fecha ?: Date())
        } catch (e: Exception) {
            timestamp.substringAfter("T").substringBefore(".")
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}