package com.thermal.monitoring.presentation.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.EventoOptimizado
import com.thermal.monitoring.data.remote.EstadisticasEventos
import com.thermal.monitoring.data.repository.AuthRepository
import com.thermal.monitoring.data.repository.EventoRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardAdminViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _eventosState = MutableLiveData<Resource<List<EventoOptimizado>>>()
    val eventosState: LiveData<Resource<List<EventoOptimizado>>> = _eventosState

    private val _estadisticasState = MutableLiveData<Resource<EstadisticasEventos>>()
    val estadisticasState: LiveData<Resource<EstadisticasEventos>> = _estadisticasState

    private val _estatusFiltro = MutableLiveData<EstatusEventoEnum?>(null)
    private val _operadorFiltro = MutableLiveData<Int?>(null)
    private val _fechaInicioFiltro = MutableLiveData<String?>(null)
    private val _fechaFinFiltro = MutableLiveData<String?>(null)

    private var cargarEstadisticasJob: Job? = null
    private var cargarEventosJob: Job? = null
    private var logoutJob: Job? = null

    init {
        cargarEstadisticas()
        cargarEventos()
    }

    fun cargarEstadisticas(fechaInicio: String? = null, fechaFin: String? = null) {
        cargarEstadisticasJob?.cancel()
        cargarEstadisticasJob = viewModelScope.launch {
            _estadisticasState.value = Resource.Loading()
            val result = eventoRepository.obtenerEstadisticas(fechaInicio, fechaFin)
            _estadisticasState.value = result
        }
    }

    fun cargarEventos() {
        cargarEventosJob?.cancel()
        cargarEventosJob = viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosOptimizado(
                estatus = _estatusFiltro.value,
                usuarioId = _operadorFiltro.value,
                fechaInicio = _fechaInicioFiltro.value,
                fechaFin = _fechaFinFiltro.value,
                limit = 100
            )
            _eventosState.value = result
        }
    }

    fun filtrarPorEstatus(estatus: EstatusEventoEnum?) {
        _estatusFiltro.value = estatus
        cargarEventos()
    }

    fun filtrarPorOperador(operadorId: Int?) {
        _operadorFiltro.value = operadorId
        cargarEventos()
    }

    fun filtrarPorRangoFechas(fechaInicio: String?, fechaFin: String?) {
        _fechaInicioFiltro.value = fechaInicio
        _fechaFinFiltro.value = fechaFin
        cargarEventos()
        cargarEstadisticas(fechaInicio, fechaFin)
    }

    fun limpiarFiltros() {
        _estatusFiltro.value = null
        _operadorFiltro.value = null
        _fechaInicioFiltro.value = null
        _fechaFinFiltro.value = null
        cargarEventos()
        cargarEstadisticas()
    }

    fun logout() {
        logoutJob?.cancel()
        logoutJob = viewModelScope.launch {
            authRepository.logout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cargarEstadisticasJob?.cancel()
        cargarEventosJob?.cancel()
        logoutJob?.cancel()
    }
}
