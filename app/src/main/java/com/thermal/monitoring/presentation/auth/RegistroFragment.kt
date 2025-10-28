package com.thermal.monitoring.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.thermal.monitoring.R
import com.thermal.monitoring.databinding.FragmentRegistroBinding
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegistrar.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmarPassword = binding.etConfirmarPassword.text.toString().trim()

            viewModel.crearUsuario(username, email, password, confirmarPassword)
        }

        binding.tvLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        // Observer para validaciones
        viewModel.validacionState.observe(viewLifecycleOwner) { validation ->
            limpiarErrores()

            validation.usernameError?.let {
                binding.tilUsername.error = it
            }
            validation.emailError?.let {
                binding.tilEmail.error = it
            }
            validation.passwordError?.let {
                binding.tilPassword.error = it
            }
            validation.confirmarPasswordError?.let {
                binding.tilConfirmarPassword.error = it
            }
        }

        // Observer para el estado de registro
        viewModel.registroState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    mostrarCargando(true)
                }
                is Resource.Success -> {
                    mostrarCargando(false)
                    Toast.makeText(
                        requireContext(),
                        "Usuario creado exitosamente. Ahora puedes iniciar sesión",
                        Toast.LENGTH_LONG
                    ).show()
                    parentFragmentManager.popBackStack()
                }
                is Resource.Error -> {
                    mostrarCargando(false)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al crear usuario",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    mostrarCargando(false)
                }
            }
        }
    }

    private fun limpiarErrores() {
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmarPassword.error = null
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.btnRegistrar.isEnabled = !mostrar
        binding.etUsername.isEnabled = !mostrar
        binding.etEmail.isEnabled = !mostrar
        binding.etPassword.isEnabled = !mostrar
        binding.etConfirmarPassword.isEnabled = !mostrar
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}