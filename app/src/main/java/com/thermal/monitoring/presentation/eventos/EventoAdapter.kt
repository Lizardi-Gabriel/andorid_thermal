package com.thermal.monitoring.presentation.eventos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.databinding.ItemEventoBinding
import java.text.SimpleDateFormat
import java.util.Locale

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
        holder.bind(getItem(position))
    }

    class EventoViewHolder(
        private val binding: ItemEventoBinding,
        private val onEventoClick: (Evento) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(evento: Evento) {
            binding.apply {
                // Formatear fecha
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvFecha.text = evento.fechaEvento

                // Mostrar hora de inicio y fin si hay imágenes
                if (evento.imagenes.isNotEmpty()) {
                    val horaInicio = evento.imagenes.first().horaSubida.substringAfter("T").substringBefore(".")
                    val horaFin = evento.imagenes.last().horaSubida.substringAfter("T").substringBefore(".")
                    tvHora.text = "$horaInicio - $horaFin"

                    // Cargar imagen preview
                    val imagenPreview = evento.imagenes.maxByOrNull { it.detecciones.size }
                    imagenPreview?.let {
                        ivPreview.load(it.rutaImagen) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_background)
                            error(R.drawable.ic_launcher_background)
                        }
                    }
                } else {
                    tvHora.text = "Sin imágenes"
                }

                // Número de detecciones
                val totalDetecciones = evento.imagenes.sumOf { it.detecciones.size }
                tvDetecciones.text = "🔥 $totalDetecciones detección(es)"

                // Descripción
                tvDescripcion.text = evento.descripcion ?: "Sin descripción"

                // Estatus
                when (evento.estatus) {
                    EstatusEventoEnum.PENDIENTE -> {
                        chipEstatus.text = "Pendiente"
                        chipEstatus.setChipBackgroundColorResource(R.color.teal_200)
                    }
                    EstatusEventoEnum.CONFIRMADO -> {
                        chipEstatus.text = "Confirmado"
                        chipEstatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                    }
                    EstatusEventoEnum.DESCARTADO -> {
                        chipEstatus.text = "Descartado"
                        chipEstatus.setChipBackgroundColorResource(android.R.color.holo_red_light)
                    }
                }

                // Click en el item
                root.setOnClickListener {
                    onEventoClick(evento)
                }
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