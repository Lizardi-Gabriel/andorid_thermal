package com.thermal.monitoring.presentation.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.data.remote.UsuarioLista
import com.thermal.monitoring.databinding.ItemUsuarioBinding

class UsuarioAdapter(
    private val onEliminarClick: (UsuarioLista) -> Unit
) : ListAdapter<UsuarioLista, UsuarioAdapter.UsuarioViewHolder>(UsuarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding, onEliminarClick)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UsuarioViewHolder(
        private val binding: ItemUsuarioBinding,
        private val onEliminarClick: (UsuarioLista) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(usuario: UsuarioLista) {
            binding.apply {
                tvNombreUsuario.text = usuario.nombreUsuario
                tvCorreo.text = usuario.correoElectronico

                chipRol.text = usuario.rol.name
                chipRol.setChipBackgroundColorResource(
                    if (usuario.rol == RolUsuarioEnum.ADMIN)
                        R.color.evento_descartado
                    else
                        R.color.amarillo
                )

                tvTotalGestionados.text = usuario.totalEventosGestionados.toString()
                tvConfirmados.text = usuario.eventosConfirmados.toString()
                tvDescartados.text = usuario.eventosDescartados.toString()

                btnEliminar.setOnClickListener {
                    onEliminarClick(usuario)
                }
            }
        }
    }

    class UsuarioDiffCallback : DiffUtil.ItemCallback<UsuarioLista>() {
        override fun areItemsTheSame(oldItem: UsuarioLista, newItem: UsuarioLista): Boolean {
            return oldItem.usuarioId == newItem.usuarioId
        }

        override fun areContentsTheSame(oldItem: UsuarioLista, newItem: UsuarioLista): Boolean {
            return oldItem == newItem
        }
    }
}