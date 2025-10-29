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
                    val horaInicio = evento.imagenes.first().horaSubida.substringAfter("T").substringBefore(".")
                    val horaFin = evento.imagenes.last().horaSubida.substringAfter("T").substringBefore(".")
                    tvHora.text = "$horaInicio - $horaFin"

                    val imagenPreview = evento.imagenes.maxByOrNull { it.detecciones.size }
                    imagenPreview?.let {
                        ivPreview.load(it.rutaImagen) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_background)
                            error(R.drawable.ic_launcher_background)
                        }
                    }
                } else {
                    tvHora.text = "Sin imagenes"
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
                promedioPm10?.let { append("PM10: %.1f ug/m3".format(it)) }
                if (promedioPm10 != null && (promedioPm2p5 != null || promedioPm1p0 != null)) append(" | ")
                promedioPm2p5?.let { append("PM2.5: %.1f ug/m3".format(it)) }
                if (promedioPm2p5 != null && promedioPm1p0 != null) append(" | ")
                promedioPm1p0?.let { append("PM1.0: %.1f ug/m3".format(it)) }
            }.takeIf { it.isNotEmpty() } ?: "Sin datos suficientes"
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