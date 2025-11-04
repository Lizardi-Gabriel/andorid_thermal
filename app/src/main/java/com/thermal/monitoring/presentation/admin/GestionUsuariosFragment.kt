package com.thermal.monitoring.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thermal.monitoring.R
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.data.remote.UsuarioLista
import com.thermal.monitoring.databinding.DialogCrearUsuarioBinding
import com.thermal.monitoring.databinding.FragmentGestionUsuariosBinding
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GestionUsuariosFragment : Fragment() {

    private var _binding: FragmentGestionUsuariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionUsuariosViewModel by viewModels()
    private lateinit var usuarioAdapter: UsuarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionUsuariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.cargarUsuarios()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        usuarioAdapter = UsuarioAdapter { usuario ->
            mostrarDialogoEliminar(usuario)
        }

        binding.rvUsuarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usuarioAdapter
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.cargarUsuarios()
        }

        binding.fabAgregarUsuario.setOnClickListener {
            mostrarDialogoCrearUsuario()
        }
    }

    private fun setupObservers() {
        viewModel.usuariosState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvVacio.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false

                    val usuarios = resource.data ?: emptyList()

                    if (usuarios.isEmpty()) {
                        binding.tvVacio.visibility = View.VISIBLE
                        binding.rvUsuarios.visibility = View.GONE
                    } else {
                        binding.tvVacio.visibility = View.GONE
                        binding.rvUsuarios.visibility = View.VISIBLE
                        usuarioAdapter.submitList(usuarios)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al cargar usuarios",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel.crearUsuarioState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Mostrar loading en dialogo si es necesario
                }
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Usuario creado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.limpiarEstadoCrear()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al crear usuario",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.limpiarEstadoCrear()
                }
                null -> { /* Estado inicial */ }
            }
        }

        viewModel.eliminarUsuarioState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Usuario eliminado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al eliminar usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> { /* Ignorar Loading */ }
            }
        }
    }

    private fun mostrarDialogoCrearUsuario() {
        val dialogBinding = DialogCrearUsuarioBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnCrear.setOnClickListener {
            val nombreUsuario = dialogBinding.etNombreUsuario.text.toString().trim()
            val correo = dialogBinding.etCorreo.text.toString().trim()
            val password = dialogBinding.etPassword.text.toString().trim()
            val rol = if (dialogBinding.rbAdmin.isChecked)
                RolUsuarioEnum.ADMIN
            else
                RolUsuarioEnum.OPERADOR

            // Validaciones
            if (nombreUsuario.isEmpty()) {
                dialogBinding.tilNombreUsuario.error = "Campo requerido"
                return@setOnClickListener
            }

            if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                dialogBinding.tilCorreo.error = "Correo invalido"
                return@setOnClickListener
            }

            if (password.length < 8) {
                dialogBinding.tilPassword.error = "Minimo 8 caracteres"
                return@setOnClickListener
            }

            viewModel.crearUsuario(nombreUsuario, correo, password, rol)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoEliminar(usuario: UsuarioLista) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estas seguro de eliminar a ${usuario.nombreUsuario}?\n\nEsta accion no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarUsuario(usuario.usuarioId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}