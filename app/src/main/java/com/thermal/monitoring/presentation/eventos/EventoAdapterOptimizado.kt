package com.thermal.monitoring.presentation.eventos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.EventoOptimizado
import com.thermal.monitoring.databinding.ItemEventoBinding

class EventoAdapterOptimizado(
    private val onEventoClick: (EventoOptimizado) -> Unit
) : ListAdapter<EventoOptimizado, EventoAdapterOptimizado.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val binding = ItemEventoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventoViewHolder(binding, onEventoClick)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventoViewHolder(
        private val binding: ItemEventoBinding,
        private val onEventoClick: (EventoOptimizado) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(evento: EventoOptimizado) {
            binding.apply {
                tvFecha.text = evento.fechaEvento

                // Usar hora_inicio y hora_fin ya calculados
                if (evento.horaInicio != null && evento.horaFin != null) {
                    tvHora.text = "${evento.horaInicio} - ${evento.horaFin}"
                } else {
                    tvHora.text = "Sin horario"
                }

                // Cargar imagen preview
                evento.imagenPreview?.let { imagen ->
                    ivPreview.load(imagen.rutaImagen) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_background)
                    }
                }

                // Max detecciones ya calculado
                tvMaxDetecciones.text = "Max: ${evento.maxDetecciones}"

                tvDescripcion.text = evento.descripcion ?: "Sin descripcion"

                // Estatus
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

                // Calidad del aire ya calculada
                tvCalidadAire.text = buildString {
                    evento.promedioPm10?.let { append("PM10: %.1f ug/m3".format(it)) }
                    if (evento.promedioPm10 != null && (evento.promedioPm2p5 != null || evento.promedioPm1p0 != null)) append(" | ")
                    evento.promedioPm2p5?.let { append("PM2.5: %.1f ug/m3".format(it)) }
                    if (evento.promedioPm2p5 != null && evento.promedioPm1p0 != null) append(" | ")
                    evento.promedioPm1p0?.let { append("PM1.0: %.1f ug/m3".format(it)) }
                }.takeIf { it.isNotEmpty() } ?: "Sin datos de calidad del aire"

                root.setOnClickListener {
                    onEventoClick(evento)
                }
            }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<EventoOptimizado>() {
        override fun areItemsTheSame(oldItem: EventoOptimizado, newItem: EventoOptimizado): Boolean {
            return oldItem.eventoId == newItem.eventoId
        }

        override fun areContentsTheSame(oldItem: EventoOptimizado, newItem: EventoOptimizado): Boolean {
            return oldItem == newItem
        }
    }
}