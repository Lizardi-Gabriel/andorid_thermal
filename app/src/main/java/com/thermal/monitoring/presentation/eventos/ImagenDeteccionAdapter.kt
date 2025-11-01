package com.thermal.monitoring.presentation.eventos

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.thermal.monitoring.data.remote.Imagen
import com.thermal.monitoring.databinding.ItemImagenDeteccionBinding

class ImagenDeteccionAdapter(
    private val imagenes: List<Imagen>
) : RecyclerView.Adapter<ImagenDeteccionAdapter.ImagenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagenViewHolder {
        val binding = ItemImagenDeteccionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImagenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagenViewHolder, position: Int) {
        holder.bind(imagenes[position])
    }

    override fun getItemCount(): Int = imagenes.size

    class ImagenViewHolder(
        private val binding: ItemImagenDeteccionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imagen: Imagen) {
            binding.progressBar.visibility = View.VISIBLE
            binding.canvasDetecciones.clear()

            binding.ivImagen.load(imagen.rutaImagen) {
                crossfade(true)
                // NO redimensionar la imagen, cargarla en su tamaño original
                size(coil.size.Size.ORIGINAL)
                listener(
                    onSuccess = { _, result ->
                        binding.progressBar.visibility = View.GONE

                        // Obtener dimensiones REALES de la imagen cargada
                        val realWidth = result.drawable.intrinsicWidth
                        val realHeight = result.drawable.intrinsicHeight

                        binding.ivImagen.post {
                            if (imagen.detecciones.isNotEmpty()) {
                                // Pasar referencia del ImageView al canvas
                                binding.canvasDetecciones.setImageView(binding.ivImagen)

                                // Pasar detecciones al canvas
                                binding.canvasDetecciones.setDetecciones(imagen.detecciones)
                            }
                        }
                    },
                    onError = { _, _ ->
                        binding.progressBar.visibility = View.GONE
                    }
                )
            }
        }

    }
}