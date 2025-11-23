package com.thermal.monitoring.presentation.admin

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thermal.monitoring.MainActivity
import com.thermal.monitoring.R
import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.data.remote.UsuarioLista
import com.thermal.monitoring.databinding.DialogCrearUsuarioBinding
import com.thermal.monitoring.databinding.DialogEditarUsuarioBinding
import com.thermal.monitoring.databinding.FragmentGestionUsuariosDrawerBinding
import com.thermal.monitoring.presentation.perfil.MiPerfilFragment
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GestionUsuariosFragment : Fragment() {

    private var _binding: FragmentGestionUsuariosDrawerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionUsuariosViewModel by viewModels()
    private lateinit var usuarioAdapter: UsuarioAdapter

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionUsuariosDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawer()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        cargarDatosUsuario()

        binding.navigationView.setCheckedItem(R.id.nav_gestion_usuarios)

        viewModel.cargarUsuarios()
    }

    private fun setupDrawer() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    navegarADashboard()
                }
                R.id.nav_gestion_usuarios -> {
                    // Ya estamos aqui
                }
                R.id.nav_generar_reporte -> {
                    navegarAGenerarReporte()
                }
                R.id.nav_perfil -> {
                    navegarAMiPerfil()
                }
                R.id.nav_logout -> {
                    cerrarSesion()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun navegarAMiPerfil() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MiPerfilFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navegarAGenerarReporte() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GenerarReporteFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val username = tokenManager.obtenerUsername().first()
            val rol = tokenManager.obtenerRol().first()

            val headerView = binding.navigationView.getHeaderView(0)
            headerView.findViewById<TextView>(R.id.tvNombreUsuario).text = username ?: "Administrador"
            headerView.findViewById<TextView>(R.id.tvRolUsuario).text = rol ?: "Admin"
        }
    }

    private fun setupRecyclerView() {
        usuarioAdapter = UsuarioAdapter(
            onEditarClick = { usuario ->
                mostrarDialogoEditar(usuario)
            },
            onEliminarClick = { usuario ->
                mostrarDialogoEliminar(usuario)
            }
        )

        binding.rvUsuarios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usuarioAdapter
        }
    }

    /**
     * Muestra un cuadro de diálogo para editar la información de un usuario existente
     * @param usuario El objeto [UsuarioLista] con los datos actuales del usuario que se va a modificar.
     * */
    private fun mostrarDialogoEditar(usuario: UsuarioLista) {
        val dialogBinding = DialogEditarUsuarioBinding.inflate(layoutInflater)

        // Pre-llenar datos actuales
        dialogBinding.etNombreUsuario.setText(usuario.nombreUsuario)
        dialogBinding.etCorreo.setText(usuario.correoElectronico)

        // Seleccionar rol actual
        if (usuario.rol == RolUsuarioEnum.ADMIN) {
            dialogBinding.rbAdmin.isChecked = true
        } else {
            dialogBinding.rbOperador.isChecked = true
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnGuardar.setOnClickListener {
            val nuevoNombre = dialogBinding.etNombreUsuario.text.toString().trim()
            val nuevoCorreo = dialogBinding.etCorreo.text.toString().trim()

            val nuevoRol = if (dialogBinding.rbAdmin.isChecked)
                RolUsuarioEnum.ADMIN
            else
                RolUsuarioEnum.OPERADOR

            // Validaciones básicas
            if (nuevoNombre.isEmpty()) {
                dialogBinding.tilNombreUsuario.error = "Campo requerido"
                return@setOnClickListener
            }

            if (nuevoCorreo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
                dialogBinding.tilCorreo.error = "Correo inválido"
                return@setOnClickListener
            }

            // Llamar al ViewModel
            viewModel.actualizarUsuario(
                usuarioId = usuario.usuarioId,
                nombreUsuario = nuevoNombre,
                correoElectronico = nuevoCorreo,
                rol = nuevoRol
            )

            dialog.dismiss()
        }

        dialog.show()
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


            viewModel.actualizarUsuarioState.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Usuario actualizado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.limpiarEstadoActualizar()
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resource.message ?: "Error al actualizar",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.limpiarEstadoActualizar()
                    }
                    null -> {}
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

            if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
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

    private fun navegarADashboard() {
        parentFragmentManager.popBackStack()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            tokenManager.limpiarDatos()
        }
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}