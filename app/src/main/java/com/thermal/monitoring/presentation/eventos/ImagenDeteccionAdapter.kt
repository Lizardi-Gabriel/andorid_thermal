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
                listener(
                    onSuccess = { _, result ->
                        binding.progressBar.visibility = View.GONE

                        val drawable = result.drawable
                        val imgWidth = drawable.intrinsicWidth
                        val imgHeight = drawable.intrinsicHeight

                        // Esperar a que el ImageView tenga sus dimensiones finales
                        binding.ivImagen.post {
                            if (imagen.detecciones.isNotEmpty()) {
                                binding.canvasDetecciones.setDetecciones(
                                    imagen.detecciones,
                                    imgWidth,
                                    imgHeight,
                                    binding.ivImagen.width,
                                    binding.ivImagen.height
                                )
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