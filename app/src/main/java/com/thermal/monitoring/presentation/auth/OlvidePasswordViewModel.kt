package com.thermal.monitoring.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.repository.PasswordRecoveryRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OlvidePasswordViewModel @Inject constructor(
    private val passwordRecoveryRepository: PasswordRecoveryRepository
) : ViewModel() {

    private val _solicitudState = MutableLiveData<Resource<String>>()
    val solicitudState: LiveData<Resource<String>> = _solicitudState

    fun solicitarRecuperacion(correoElectronico: String) {
        viewModelScope.launch {
            _solicitudState.value = Resource.Loading()
            val result = passwordRecoveryRepository.solicitarRecuperacion(correoElectronico)
            _solicitudState.value = result
        }
    }

    fun limpiarEstado() {
        _solicitudState.value = null
    }

}