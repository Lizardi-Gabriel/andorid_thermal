package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.remote.EstatusEventoEnum
import com.thermal.monitoring.data.remote.Evento
import com.thermal.monitoring.data.remote.EventoDetalleOptimizado
import com.thermal.monitoring.data.remote.EventoOptimizado
import com.thermal.monitoring.data.remote.EventoService
import com.thermal.monitoring.data.remote.EstadisticasEventos
import com.thermal.monitoring.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventoRepository @Inject constructor(
    private val eventoService: EventoService
) {

    suspend fun listarEventosOptimizado(
        estatus: EstatusEventoEnum? = null,
        usuarioId: Int? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        skip: Int = 0,
        limit: Int = 1000
    ): Resource<List<EventoOptimizado>> {
        return try {
            val response = eventoService.listarEventosOptimizado(
                estatus = estatus?.name?.lowercase(),
                usuarioId = usuarioId,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                skip = skip,
                limit = limit
            )

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al obtener eventos")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun obtenerEventoOptimizado(eventoId: Int): Resource<EventoDetalleOptimizado> {
        return try {
            val response = eventoService.obtenerEventoOptimizado(eventoId)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Evento no encontrado")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun obtenerEstadisticas(
        fechaInicio: String? = null,
        fechaFin: String? = null
    ): Resource<EstadisticasEventos> {
        return try {
            val response = eventoService.obtenerEstadisticas(fechaInicio, fechaFin)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al obtener estadisticas")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun actualizarEstatusEvento(eventoId: Int, estatus: EstatusEventoEnum): Resource<Evento> {
        return try {
            val response = eventoService.actualizarEstatusEvento(eventoId, estatus)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al actualizar evento")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }
}