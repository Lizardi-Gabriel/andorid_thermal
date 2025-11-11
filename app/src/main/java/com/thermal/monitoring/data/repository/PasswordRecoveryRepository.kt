package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.remote.AuthService
import com.thermal.monitoring.data.remote.RestablecerPassword
import com.thermal.monitoring.data.remote.RestablecerPasswordResponse
import com.thermal.monitoring.data.remote.SolicitudRecuperacionPassword
import com.thermal.monitoring.data.remote.ValidarTokenResponse
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

    suspend fun validarToken(token: String): Resource<ValidarTokenResponse> {
        return try {
            val response = authService.validarTokenRecuperacion(token)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al validar token")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun restablecerPassword(token: String, nuevaPassword: String): Resource<RestablecerPasswordResponse> {
        return try {
            val datos = RestablecerPassword(token, nuevaPassword)
            val response = authService.restablecerPassword(datos)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Token invalido o expirado"
                    404 -> "Usuario no encontrado"
                    else -> "Error al restablecer contraseña"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }
}