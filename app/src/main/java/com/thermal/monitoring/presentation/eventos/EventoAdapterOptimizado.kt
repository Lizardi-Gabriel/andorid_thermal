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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.text.SpannableStringBuilder
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

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

                if (evento.horaInicio != null && evento.horaFin != null) {
                    val horaInicio = convertirAHoraMexico(evento.horaInicio)
                    val horaFin = convertirAHoraMexico(evento.horaFin)
                    tvHora.text = "${horaInicio} - ${horaFin}"
                } else {
                    tvHora.text = "Sin horario"
                }

                evento.imagenPreview?.let { imagen ->
                    ivPreview.load(imagen.rutaImagen) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_background)
                    }
                }

                tvMaxDetecciones.text = "Max: ${evento.maxDetecciones}"
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

                mostrarCalidadAireConColores(evento)

                root.setOnClickListener {
                    onEventoClick(evento)
                }
            }
        }

        private fun mostrarCalidadAireConColores(evento: EventoOptimizado) {
            val pm10 = evento.promedioPm10?.toDouble() ?: 0.0
            val pm25 = evento.promedioPm2p5?.toDouble() ?: 0.0
            val pm1 = evento.promedioPm1p0?.toDouble() ?: 0.0

            if (pm10 == 0.0 && pm25 == 0.0 && pm1 == 0.0) {
                binding.tvCalidadAire.text = "Sin datos de calidad del aire"
                return
            }

            val spannable = SpannableStringBuilder()
            var needsSeparator = false

            if (pm10 > 0.0) {
                appendWithColor(spannable, pm10, "PM10", 45.0, 150.0)
                needsSeparator = true
            }

            if (pm25 > 0.0) {
                if (needsSeparator) spannable.append(" | ")
                appendWithColor(spannable, pm25, "PM2.5", 15.0, 55.0)
                needsSeparator = true
            }

            if (pm1 > 0.0) {
                if (needsSeparator) spannable.append(" | ")
                appendWithColor(spannable, pm1, "PM1.0", 10.0, 35.0)
            }

            binding.tvCalidadAire.text = spannable
        }

        private fun appendWithColor(
            spannable: SpannableStringBuilder,
            valor: Double,
            etiqueta: String,
            limiteAmarillo: Double,
            limiteRojo: Double
        ) {
            val texto = "$etiqueta: %.1f".format(valor)
            val start = spannable.length
            spannable.append(texto)
            val end = spannable.length

            val color = when {
                valor >= limiteRojo -> R.color.rojo
                valor >= limiteAmarillo -> R.color.amarillo
                else -> android.R.color.holo_green_dark
            }

            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(binding.root.context, color)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
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