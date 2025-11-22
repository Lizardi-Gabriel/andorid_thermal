package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.remote.AuthService
import com.thermal.monitoring.data.remote.SolicitudRecuperacionPassword
import com.thermal.monitoring.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordRecoveryRepository @Inject constructor(private val authService: AuthService) {

    suspend fun solicitarRecuperacion(correoElectronico: String): Resource<String> {
        return try {
            val solicitud = SolicitudRecuperacionPassword(correoElectronico)
            val response = authService.solicitarRecuperacionPassword(solicitud)

            if (response.isSuccessful && response.body() != null) {
                val mensaje = response.body()?.get("mensaje") ?: "Solicitud enviada"
                Resource.Success(mensaje)
            } else {
                Resource.Error("Error al solicitar recuperacion")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }



}