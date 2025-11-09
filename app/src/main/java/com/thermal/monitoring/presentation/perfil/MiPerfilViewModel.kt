package com.thermal.monitoring.presentation.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.EstadisticasUsuario
import com.thermal.monitoring.data.repository.AdminRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiPerfilViewModel @Inject constructor(private val adminRepository: AdminRepository) : ViewModel() {

    private val _estadisticasState = MutableLiveData<Resource<EstadisticasUsuario>>()
    val estadisticasState: LiveData<Resource<EstadisticasUsuario>> = _estadisticasState

    fun cargarEstadisticasUsuario(usuarioId: Int) {
        viewModelScope.launch {
            _estadisticasState.value = Resource.Loading()
            val result = adminRepository.obtenerEstadisticasUsuario(usuarioId)
            _estadisticasState.value = result
        }
    }
}