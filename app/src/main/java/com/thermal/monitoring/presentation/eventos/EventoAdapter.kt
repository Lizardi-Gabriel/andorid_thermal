package com.thermal.monitoring.presentation.eventos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.databinding.ItemEventoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EventoAdapter(
    private val onEventoClick: (Evento) -> Unit
) : ListAdapter<Evento, EventoAdapter.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventoViewHolder(binding, onEventoClick)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class EventoViewHolder(
        private val binding: ItemEventoBinding,
        private val onEventoClick: (Evento) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(evento: Evento, numeroEvento: Int) {
            binding.apply {
                // Agregar numero de evento
                tvFecha.text = "${evento.fechaEvento}"
                // No $numeroEvento

                if (evento.imagenes.isNotEmpty()) {
                    val horaInicio = convertirUTCaMexico(evento.imagenes.first().horaSubida)
                    val horaFin = convertirUTCaMexico(evento.imagenes.last().horaSubida)
                    tvHora.text = "$horaInicio - $horaFin"

                    val imagenPreview = evento.imagenes.maxByOrNull { it.detecciones.size } ?: evento.imagenes.firstOrNull()
                    imagenPreview?.let {
                        ivPreview.load(it.rutaImagen) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_background)
                            error(R.drawable.ic_launcher_background)
                        }
                    }
                } else {
                    tvHora.text = "Sin imagenes"
                    ivPreview.load(R.drawable.ic_launcher_background) {
                        error(R.drawable.ic_launcher_background)
                    }

                }

                val maxDetecciones = evento.imagenes.maxOfOrNull { it.detecciones.size } ?: 0
                tvMaxDetecciones.text = "Max: $maxDetecciones"

                tvDescripcion.text = evento.descripcion ?: "Sin descripcion"

                when (evento.estatus) {
                    EstatusEventoEnum.PENDIENTE -> {
                        chipEstatus.text = "Pendiente"
                        chipEstatus.setChipBackgroundColorResource(R.color.evento_pendiente)
                    }
                    EstatusEventoEnum.CONFIRMADO -> {
                        chipEstatus.text = "Confirmado"
                        chipEstatus.setChipBackgroundColorResource(R.color.evento_confirmado)
                    }
                    EstatusEventoEnum.DESCARTADO -> {
                        chipEstatus.text = "Descartado"
                        chipEstatus.setChipBackgroundColorResource(R.color.evento_descartado)
                    }
                }

                tvCalidadAire.text = calcularPromediosCalidadAire(evento)

                root.setOnClickListener {
                    onEventoClick(evento)
                }
            }
        }

        private fun calcularPromediosCalidadAire(evento: Evento): String {
            if (evento.registrosCalidadAire.isEmpty()) {
                return "Sin datos de calidad del aire"
            }

            val registrosPorHora = mutableMapOf<String, com.thermal.monitoring.data.remote.CalidadAire>()

            evento.registrosCalidadAire.forEach { registro ->
                val hora = registro.horaMedicion?.substringBefore(".")?.substringAfter("T") ?: ""
                if (hora.isNotEmpty() && !registrosPorHora.containsKey(hora)) {
                    registrosPorHora[hora] = registro
                }
            }

            val registrosUnicos = registrosPorHora.values.toList()

            val pm10Values = registrosUnicos.mapNotNull { it.pm10 }
            val pm2p5Values = registrosUnicos.mapNotNull { it.pm2p5 }
            val pm1p0Values = registrosUnicos.mapNotNull { it.pm1p0 }

            val promedioPm10 = if (pm10Values.isNotEmpty()) pm10Values.average() else null
            val promedioPm2p5 = if (pm2p5Values.isNotEmpty()) pm2p5Values.average() else null
            val promedioPm1p0 = if (pm1p0Values.isNotEmpty()) pm1p0Values.average() else null

            return buildString {
                append("Calidad del aire: (ug/m3)\n")
                promedioPm10?.let { append("PM 10: %.1f ".format(it)) }
                if (promedioPm10 != null && (promedioPm2p5 != null || promedioPm1p0 != null)) append(" | ")
                promedioPm2p5?.let { append("PM 2.5: %.1f ".format(it)) }
                if (promedioPm2p5 != null && promedioPm1p0 != null) append(" | ")
                promedioPm1p0?.let { append("PM 1: %.1f ".format(it)) }
            }.takeIf { it.isNotEmpty() } ?: "Sin datos suficientes"
        }

        private fun convertirUTCaMexico(fechaUTC: String): String {
            return try {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                formatoEntrada.timeZone = TimeZone.getTimeZone("UTC")

                val fecha = formatoEntrada.parse(fechaUTC)

                val formatoSalida = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                formatoSalida.timeZone = TimeZone.getTimeZone("America/Mexico_City")

                formatoSalida.format(fecha ?: Date())
            } catch (e: Exception) {
                "00:00:00"
            }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento): Boolean {
            return oldItem.eventoId == newItem.eventoId
        }

        override fun areContentsTheSame(oldItem: Evento, newItem: Evento): Boolean {
            return oldItem == newItem
        }
    }
}