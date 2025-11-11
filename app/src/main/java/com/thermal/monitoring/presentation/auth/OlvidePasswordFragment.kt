package com.thermal.monitoring.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thermal.monitoring.R
import com.thermal.monitoring.databinding.FragmentOlvidePasswordBinding
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OlvidePasswordFragment : Fragment() {

    private var _binding: FragmentOlvidePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OlvidePasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOlvidePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnEnviar.setOnClickListener {
            solicitarRecuperacion()
        }

        binding.btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.solicitudState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnEnviar.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnEnviar.isEnabled = true

                    mostrarDialogoExito(resource.data ?: "Correo enviado")

                    viewModel.limpiarEstado()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnEnviar.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Error al enviar solicitud",
                        Toast.LENGTH_LONG
                    ).show()

                    viewModel.limpiarEstado()
                }
                null -> { /* Estado inicial */ }
            }
        }
    }

    private fun solicitarRecuperacion() {
        val correo = binding.etCorreo.text.toString().trim()

        // Validaciones
        if (correo.isEmpty()) {
            binding.tilCorreo.error = "Campo requerido"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.error = "Correo invalido"
            return
        }

        binding.tilCorreo.error = null

        viewModel.solicitarRecuperacion(correo)
    }

    private fun mostrarDialogoExito(mensaje: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Solicitud Enviada")
            .setMessage("$mensaje\n\nRevisa tu correo electronico y sigue las instrucciones para restablecer tu contraseña.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}