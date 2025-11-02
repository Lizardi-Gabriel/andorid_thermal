package com.thermal.monitoring.presentation.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.data.repository.AuthRepository
import com.thermal.monitoring.data.repository.EventoRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardOperadorViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _eventosState = MutableLiveData<Resource<List<Evento>>>()
    val eventosState: LiveData<Resource<List<Evento>>> = _eventosState

    private val _eventosPendientes = MutableLiveData<Int>()
    val eventosPendientes: LiveData<Int> = _eventosPendientes

    private val _filtroActual = MutableLiveData<FiltroEvento>(FiltroEvento.TODOS)
    val filtroActual: LiveData<FiltroEvento> = _filtroActual

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventos(limit = 10000)
            _eventosState.value = result

            if (result is Resource.Success) {
                val pendientes = result.data?.count { it.estatus == EstatusEventoEnum.PENDIENTE } ?: 0
                _eventosPendientes.value = pendientes
            }
        }
    }

    fun cargarEventosPorFecha(fecha: String) {
        viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosPorFecha(fecha)
            _eventosState.value = result

            if (result is Resource.Success) {
                val pendientes = result.data?.count { it.estatus == EstatusEventoEnum.PENDIENTE } ?: 0
                _eventosPendientes.value = pendientes
            }
        }
    }

    fun cargarEventosPendientes() {
        viewModelScope.launch {
            _filtroActual.value = FiltroEvento.PENDIENTES
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventos(limit = 10000)

            if (result is Resource.Success) {
                val eventosPendientes = result.data?.filter { it.estatus == EstatusEventoEnum.PENDIENTE } ?: emptyList()
                _eventosState.value = Resource.Success(eventosPendientes)
                _eventosPendientes.value = eventosPendientes.size
            } else {
                _eventosState.value = result
            }
        }
    }

    fun cargarMiHistorial(usuarioId: Int) {
        viewModelScope.launch {
            _filtroActual.value = FiltroEvento.MI_HISTORIAL
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventos(limit = 10000)

            if (result is Resource.Success) {
                val miHistorial = result.data?.filter {
                    it.usuarioId == usuarioId && it.estatus != EstatusEventoEnum.PENDIENTE
                } ?: emptyList()
                _eventosState.value = Resource.Success(miHistorial)
                _eventosPendientes.value = 0
            } else {
                _eventosState.value = result
            }
        }
    }

    fun cargarTodos() {
        _filtroActual.value = FiltroEvento.TODOS
        cargarEventos()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    enum class FiltroEvento {
        TODOS,
        PENDIENTES,
        MI_HISTORIAL
    }
}