package com.thermal.monitoring.presentation.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.data.repository.EventoRepository
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleEventoViewModel @Inject constructor(
    private val eventoRepository: EventoRepository
) : ViewModel() {

    private val _eventoState = MutableLiveData<Resource<Evento>>()
    val eventoState: LiveData<Resource<Evento>> = _eventoState

    private val _actualizarEstatusState = MutableLiveData<Resource<Evento>>()
    val actualizarEstatusState: LiveData<Resource<Evento>> = _actualizarEstatusState

    fun cargarEvento(eventoId: Int) {
        viewModelScope.launch {
            _eventoState.value = Resource.Loading()
            val result = eventoRepository.obtenerEvento(eventoId)
            _eventoState.value = result
        }
    }

    fun confirmarEvento(eventoId: Int) {
        viewModelScope.launch {
            _actualizarEstatusState.value = Resource.Loading()
            val result = eventoRepository.actualizarEstatusEvento(
                eventoId,
                EstatusEventoEnum.CONFIRMADO
            )
            _actualizarEstatusState.value = result

            // Recargar el evento actualizado
            if (result is Resource.Success) {
                cargarEvento(eventoId)
            }
        }
    }

    fun descartarEvento(eventoId: Int) {
        viewModelScope.launch {
            _actualizarEstatusState.value = Resource.Loading()
            val result = eventoRepository.actualizarEstatusEvento(
                eventoId,
                EstatusEventoEnum.DESCARTADO
            )
            _actualizarEstatusState.value = result

            // Recargar el evento actualizado
            if (result is Resource.Success) {
                cargarEvento(eventoId)
            }
        }
    }
}