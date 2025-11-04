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
import kotlinx.coroutines.Job
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

    private var usuarioIdActual: Int? = null

    private var recargarEventosJob: Job? = null
    private var logoutJob: Job? = null


    fun setUsuarioId(usuarioId: Int) {
        usuarioIdActual = usuarioId
    }

    fun recargarEventos() {
        recargarEventosJob?.cancel()

        val filtro = _filtroActual.value
        val fecha = _fechaSeleccionada.value

        val estatus = if (filtro == FiltroEvento.PENDIENTES) EstatusEventoEnum.PENDIENTE else null
        val usuarioId = if (filtro == FiltroEvento.MI_HISTORIAL) usuarioIdActual else null

        if (filtro == FiltroEvento.MI_HISTORIAL && usuarioId == null) {
            _eventosState.value = Resource.Success(emptyList())
            return
        }

        recargarEventosJob = viewModelScope.launch {
            _eventosState.value = Resource.Loading()
            val result = eventoRepository.listarEventosOptimizado(
                usuarioId = usuarioId,
                estatus = estatus,
                fechaInicio = fecha,
                fechaFin = fecha,
                limit = 50
            )

            if (result is Resource.Success) {
                var eventos = result.data ?: emptyList()

                if (filtro == FiltroEvento.MI_HISTORIAL) {
                    eventos = eventos.filter { it.estatus != EstatusEventoEnum.PENDIENTE }
                    _eventosPendientes.value = 0
                } else {
                    val pendientes = eventos.count { it.estatus == EstatusEventoEnum.PENDIENTE }
                    _eventosPendientes.value = pendientes
                }

                eventos = try {
                    eventos.sortedWith(
                        compareByDescending<EventoOptimizado> { it.fechaEvento }
                            .thenByDescending { it.horaInicio }
                    )
                } catch (e: Exception) {
                    eventos.sortedByDescending { it.horaInicio }
                }

                _eventosState.value = Resource.Success(eventos)

            } else {
                _eventosState.value = result
            }
        }
    }


    fun cargarEventosPorFecha(fecha: String) {
        _fechaSeleccionada.value = fecha
        recargarEventos()
    }

    fun cargarEventosPendientes() {
        _filtroActual.value = FiltroEvento.PENDIENTES
        recargarEventos()
    }

    fun cargarMiHistorial() {
        _filtroActual.value = FiltroEvento.MI_HISTORIAL
        recargarEventos()
    }

    fun cargarTodos() {
        _filtroActual.value = FiltroEvento.TODOS
        recargarEventos()
    }

    fun limpiarFiltroFecha() {
        _fechaSeleccionada.value = null
        recargarEventos()
    }

    fun logout() {
        logoutJob?.cancel()
        logoutJob = viewModelScope.launch {
            authRepository.desactivarTokenFCM()
            authRepository.logout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        recargarEventosJob?.cancel()
        logoutJob?.cancel()
    }

    enum class FiltroEvento {
        TODOS,
        PENDIENTES,
        MI_HISTORIAL
    }
}
