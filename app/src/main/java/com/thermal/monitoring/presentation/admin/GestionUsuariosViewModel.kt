package com.thermal.monitoring.presentation.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.data.remote.Usuario
import com.thermal.monitoring.data.remote.UsuarioLista
import com.thermal.monitoring.data.repository.AdminRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestionUsuariosViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _usuariosState = MutableLiveData<Resource<List<UsuarioLista>>>()
    val usuariosState: LiveData<Resource<List<UsuarioLista>>> = _usuariosState

    private val _crearUsuarioState = MutableLiveData<Resource<Usuario>>()
    val crearUsuarioState: LiveData<Resource<Usuario>> = _crearUsuarioState

    private val _eliminarUsuarioState = MutableLiveData<Resource<Unit>>()
    val eliminarUsuarioState: LiveData<Resource<Unit>> = _eliminarUsuarioState

    private val _actualizarUsuarioState = MutableLiveData<Resource<Usuario>>()
    val actualizarUsuarioState: LiveData<Resource<Usuario>> = _actualizarUsuarioState

    fun cargarUsuarios() {
        viewModelScope.launch {
            _usuariosState.value = Resource.Loading()
            val result = adminRepository.listarUsuarios()
            _usuariosState.value = result
        }
    }

    fun crearUsuario(
        nombreUsuario: String,
        correoElectronico: String,
        password: String,
        rol: RolUsuarioEnum
    ) {
        viewModelScope.launch {
            _crearUsuarioState.value = Resource.Loading()
            val result = adminRepository.crearUsuario(
                nombreUsuario,
                correoElectronico,
                password,
                rol
            )
            _crearUsuarioState.value = result

            if (result is Resource.Success) {
                cargarUsuarios()
            }
        }
    }

    fun eliminarUsuario(usuarioId: Int) {
        viewModelScope.launch {
            _eliminarUsuarioState.value = Resource.Loading()
            val result = adminRepository.eliminarUsuario(usuarioId)
            _eliminarUsuarioState.value = result

            if (result is Resource.Success) {
                cargarUsuarios()
            }
        }
    }

    fun limpiarEstadoCrear() {
        _crearUsuarioState.value = null
    }


    /**
     * Actualiza la informacion de un usuario existente
     **/
    fun actualizarUsuario(
        usuarioId: Int,
        nombreUsuario: String,
        correoElectronico: String,
        rol: RolUsuarioEnum
    ) {
        viewModelScope.launch {
            _actualizarUsuarioState.value = Resource.Loading()

            // TOOD: verificar no nulos
            if (nombreUsuario.isNullOrBlank() || correoElectronico.isNullOrBlank()) {
                _actualizarUsuarioState.value = Resource.Error("Campos vacios")
                return@launch
            }

            val result = adminRepository.actualizarUsuario(
                usuarioId,
                nombreUsuario,
                correoElectronico,
                rol
            )
            _actualizarUsuarioState.value = result

            if (result is Resource.Success) {
                cargarUsuarios()
            }
        }
    }

    fun limpiarEstadoActualizar() {
        _actualizarUsuarioState.value = null
    }

}