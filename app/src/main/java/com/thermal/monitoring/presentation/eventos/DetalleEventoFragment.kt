package com.thermal.monitoring.presentation.eventos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thermal.monitoring.R
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.data.remote.EventoDetalleOptimizado
import com.thermal.monitoring.databinding.FragmentDetalleEventoBinding
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DetalleEventoFragment : Fragment() {

    private var _binding: FragmentDetalleEventoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetalleEventoViewModel by viewModels()
    private var eventoId: Int = -1
    private var esOperador: Boolean = true

    @Inject
    lateinit var tokenManager: TokenManager

    companion object {
        private const val ARG_EVENTO_ID = "evento_id"
        private const val ARG_ES_ADMIN = "es_admin"

        fun newInstance(eventoId: Int, esAdmin: Boolean = false) = DetalleEventoFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_EVENTO_ID, eventoId)
                putBoolean(ARG_ES_ADMIN, esAdmin)
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

        eventoId = arguments?.getInt(ARG_EVENTO_ID) ?: -1
        val esAdmin = arguments?.getBoolean(ARG_ES_ADMIN, false) ?: false

        lifecycleScope.launch {
            val rol = tokenManager.obtenerRol().first()
            esOperador = rol == "OPERADOR" && !esAdmin
        }

        if (eventoId == -1) {
            Toast.makeText(requireContext(), "Error: ID de evento invalido", Toast.LENGTH_SHORT).show()
            activity?.onBackPressed()
            return
        }

        setupObservers()
        viewModel.cargarEvento(eventoId)
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

    private fun mostrarEvento(evento: EventoDetalleOptimizado) {
        binding.apply {
            tvEventoId.text = "Evento #${evento.eventoId}"
            tvFecha.text = "Fecha: ${evento.fechaEvento}"

            // Usar horas ya calculadas
            if (evento.horaInicio != null && evento.horaFin != null) {
                val horaInicio = convertirAHoraMexico(evento.horaInicio)
                val horaFin = convertirAHoraMexico(evento.horaFin)

                tvHorario.text = "Horario: ${horaInicio} - ${horaFin}"
            } else {
                tvHorario.text = "Horario: Sin horario"
            }

            // Total detecciones ya calculado
            tvDetecciones.text = "Detecciones: ${evento.totalDetecciones}"

            configurarEstatus(evento.estatus)

            tvDescripcion.text = evento.descripcion ?: "Sin descripcion disponible"

            if (evento.usuario != null) {
                tvGestionadoPor.visibility = View.VISIBLE
                tvGestionadoPor.text = "Gestionado por: ${evento.usuario.nombreUsuario}"
            }

            // Galería de imágenes
            if (evento.imagenes.isNotEmpty()) {
                val adapter = ImagenDeteccionAdapter(evento.imagenes)
                viewPagerImagenes.adapter = adapter

                tvContadorImagenes.text = "Imagen 1 / ${evento.totalImagenes} | ${evento.totalImagenes} fotos | Max: ${evento.maxDetecciones} | Actual: ${evento.imagenes[0].detecciones.size}"

                viewPagerImagenes.registerOnPageChangeCallback(
                    object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            val deteccionesEnEstaImagen = evento.imagenes[position].detecciones.size
                            tvContadorImagenes.text = "Imagen ${position + 1} / ${evento.totalImagenes} | ${evento.totalImagenes} fotos | Max: ${evento.maxDetecciones} | Actual: $deteccionesEnEstaImagen"
                        }
                    }
                )
            } else {
                tvContadorImagenes.text = "Sin imagenes"
            }

            // Calidad del aire ya calculada
            mostrarCalidadAireOptimizada(evento)

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

    private fun mostrarCalidadAireOptimizada(evento: EventoDetalleOptimizado) {
        val texto = buildString {
            append("Promedio de calidad del aire durante este evento:\n\n")

            evento.promedioPm10?.let {
                append("PM10: %.2f microgramos/m3\n".format(it))
            }
            evento.promedioPm2p5?.let {
                append("PM2.5: %.2f microgramos/m3\n".format(it))
            }
            evento.promedioPm1p0?.let {
                append("PM1.0: %.2f microgramos/m3\n".format(it))
            }

            if (evento.promedioPm10 == null && evento.promedioPm2p5 == null && evento.promedioPm1p0 == null) {
                append("No hay datos suficientes")
            }
        }

        binding.tvCalidadAire.text = texto.trim()
    }


    private fun setupBotonesAccion(evento: EventoDetalleOptimizado) {
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


    private fun convertirAHoraMexico(horaUtc: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")
            val fechaUtc = formatoEntrada.parse(horaUtc)
            val formatoSalida = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            formatoSalida.timeZone = TimeZone.getTimeZone("America/Mexico_City")
            formatoSalida.format(fechaUtc ?: Date())
        } catch (e: Exception) {
            horaUtc
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}