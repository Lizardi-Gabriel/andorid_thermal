package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.remote.EstadisticasUsuario
import com.thermal.monitoring.data.remote.ProfileService
import com.thermal.monitoring.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ProfileRepository @Inject constructor(
    private val profileService: ProfileService
) {

    suspend fun obtenerEstadisticasUsuario(usuarioId: Int): Resource<EstadisticasUsuario> {
        return try {
            val response = profileService.obtenerEstadisticasUsuario(usuarioId)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al obtener estadisticas")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }


}