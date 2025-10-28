package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.data.remote.EventoService
import com.thermal.monitoring.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventoRepository @Inject constructor(
    private val eventoService: EventoService
) {

    // Obtener lista de eventos
    suspend fun listarEventos(skip: Int = 0, limit: Int = 25): Resource<List<Evento>> {
        return try {
            val response = eventoService.listarEventos(skip, limit)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al obtener eventos")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Obtener eventos por fecha
    suspend fun listarEventosPorFecha(fecha: String): Resource<List<Evento>> {
        return try {
            val response = eventoService.listarEventosPorFecha(fecha)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al obtener eventos")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Obtener detalle de un evento
    suspend fun obtenerEvento(eventoId: Int): Resource<Evento> {
        return try {
            val response = eventoService.obtenerEvento(eventoId)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Evento no encontrado")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Actualizar estatus de un evento
    suspend fun actualizarEstatusEvento(eventoId: Int, estatus: EstatusEventoEnum): Resource<Evento> {
        return try {
            val response = eventoService.actualizarEstatusEvento(eventoId, estatus)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al actualizar evento")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
}