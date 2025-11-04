package com.thermal.monitoring.presentation.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.repository.AdminRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _reportePDFState = MutableLiveData<Resource<ResponseBody>>()
    val reportePDFState: LiveData<Resource<ResponseBody>> = _reportePDFState

    fun generarReportePDF(fechaInicio: String? = null, fechaFin: String? = null) {
        viewModelScope.launch {
            _reportePDFState.value = Resource.Loading()
            val result = adminRepository.generarReportePDF(fechaInicio, fechaFin)
            _reportePDFState.value = result
        }
    }

    fun limpiarEstado() {
        _reportePDFState.value = null
    }
}