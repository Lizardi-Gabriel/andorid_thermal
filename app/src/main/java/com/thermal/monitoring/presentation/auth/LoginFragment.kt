package com.thermal.monitoring.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.thermal.monitoring.R
import com.thermal.monitoring.databinding.FragmentLoginBinding
import com.thermal.monitoring.presentation.admin.DashboardAdminFragment
import com.thermal.monitoring.presentation.dashboard.DashboardOperadorFragment
import com.thermal.monitoring.utils.NotificationHelper
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(username, password)
        }

//        binding.tvRegistro.setOnClickListener {
//            navegarARegistro()
//        }

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnOlvidePassword.setOnClickListener {
            navegarAOlvidePassword()
        }

    }

    private fun navegarAOlvidePassword() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OlvidePasswordFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    mostrarCargando(true)
                }

                is Resource.Success -> {
                    mostrarCargando(false)
                    val usuario = resource.data
                    Toast.makeText(
                        requireContext(),
                        "Bienvenido ${usuario?.nombreUsuario}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Registrar token FCM
                    NotificationHelper.obtenerTokenFCM { token ->
                        viewModel.registrarTokenFCM(token)
                    }

                    // Navegar al dashboard segun el rol
                    navegarADashboard(usuario?.rol?.name)
                }
                is Resource.Error -> {
                    mostrarCargando(false)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error desconocido",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    mostrarCargando(false)
                }
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !mostrar
        binding.etUsername.isEnabled = !mostrar
        binding.etPassword.isEnabled = !mostrar
    }

    private fun navegarARegistro() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, RegistroFragment())
            .addToBackStack("registro")
            .commit()
    }

    private fun navegarADashboard(rol: String?) {
        when (rol) {
            "OPERADOR" -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardOperadorFragment())
                    .commit()
            }
            "ADMIN" -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardAdminFragment())
                    .commit()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Rol desconocido: $rol",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}