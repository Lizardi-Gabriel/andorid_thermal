package com.thermal.monitoring.presentation.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.EventoOptimizado
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

    private val _eventosState = MutableLiveData<Resource<List<EventoOptimizado>>>()
    val eventosState: LiveData<Resource<List<EventoOptimizado>>> = _eventosState

    private val _eventosPendientes = MutableLiveData<Int>()
    val eventosPendientes: LiveData<Int> = _eventosPendientes

    private val _filtroActual = MutableLiveData<FiltroEvento>(FiltroEvento.TODOS)
    val filtroActual: LiveData<FiltroEvento> = _filtroActual

    private val _fechaSeleccionada = MutableLiveData<String?>(null)
    val fechaSeleccionada: LiveData<String?> = _fechaSeleccionada

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        // Esta función ahora solo carga eventos, no cambia el filtro
        viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosOptimizado(limit = 50)
            _eventosState.value = result

            if (result is Resource.Success) {
                val pendientes = result.data?.count { it.estatus == EstatusEventoEnum.PENDIENTE } ?: 0
                _eventosPendientes.value = pendientes
            }
        }
    }

    fun cargarEventosPorFecha(fecha: String) {
        _filtroActual.value = FiltroEvento.POR_FECHA
        _fechaSeleccionada.value = fecha

        viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosOptimizado(
                fechaInicio = fecha,
                fechaFin = fecha,
                limit = 50
            )
            _eventosState.value = result

            if (result is Resource.Success) {
                val pendientes = result.data?.count { it.estatus == EstatusEventoEnum.PENDIENTE } ?: 0
                _eventosPendientes.value = pendientes
            }
        }
    }

    fun cargarEventosPendientes() {
        _fechaSeleccionada.value = null
        _filtroActual.value = FiltroEvento.PENDIENTES

        viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosOptimizado(
                estatus = EstatusEventoEnum.PENDIENTE,
                limit = 50
            )
            _eventosState.value = result

            if (result is Resource.Success) {
                _eventosPendientes.value = result.data?.size ?: 0
            }
        }
    }

    fun cargarMiHistorial(usuarioId: Int) {
        _fechaSeleccionada.value = null
        _filtroActual.value = FiltroEvento.MI_HISTORIAL

        viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosOptimizado(
                usuarioId = usuarioId,
                limit = 50
            )

            if (result is Resource.Success) {
                // Filtrar solo confirmados y descartados
                val miHistorial = result.data?.filter {
                    it.estatus != EstatusEventoEnum.PENDIENTE
                } ?: emptyList()
                _eventosState.value = Resource.Success(miHistorial)
                _eventosPendientes.value = 0
            } else {
                _eventosState.value = result
            }
        }
    }

    fun cargarTodos() {
        _fechaSeleccionada.value = null
        _filtroActual.value = FiltroEvento.TODOS
        cargarEventos()
    }

    fun logout() {
        viewModelScope.launch {
            // Desactivar token FCM antes de cerrar sesión
            authRepository.desactivarTokenFCM()
            authRepository.logout()
        }
    }

    enum class FiltroEvento {
        TODOS,
        PENDIENTES,
        MI_HISTORIAL,
        POR_FECHA
    }
}
