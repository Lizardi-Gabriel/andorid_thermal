package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.remote.AdminService
import com.thermal.monitoring.data.remote.EstadisticasUsuario
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.data.remote.Usuario
import com.thermal.monitoring.data.remote.UsuarioCreateRequest
import com.thermal.monitoring.data.remote.UsuarioLista
import com.thermal.monitoring.data.remote.UsuarioUpdateRequest
import com.thermal.monitoring.utils.Resource
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val adminService: AdminService
) {

    suspend fun listarUsuarios(): Resource<List<UsuarioLista>> {
        return try {
            val response = adminService.listarUsuarios()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al obtener usuarios")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun crearUsuario(
        nombreUsuario: String,
        correoElectronico: String,
        password: String,
        rol: RolUsuarioEnum
    ): Resource<Usuario> {
        return try {
            val request = UsuarioCreateRequest(
                nombreUsuario = nombreUsuario,
                correoElectronico = correoElectronico,
                password = password,
                rol = rol
            )

            val response = adminService.crearUsuario(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "El usuario o correo ya existe"
                    else -> "Error al crear usuario"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun actualizarUsuario(
        usuarioId: Int,
        nombreUsuario: String?,
        correoElectronico: String?,
        password: String?,
        rol: RolUsuarioEnum?
    ): Resource<Usuario> {
        return try {
            val request = UsuarioUpdateRequest(
                nombreUsuario = nombreUsuario,
                correoElectronico = correoElectronico,
                password = password,
                rol = rol
            )

            val response = adminService.actualizarUsuario(usuarioId, request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al actualizar usuario")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun eliminarUsuario(usuarioId: Int): Resource<Unit> {
        return try {
            val response = adminService.eliminarUsuario(usuarioId)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "No puedes eliminar tu propia cuenta"
                    404 -> "Usuario no encontrado"
                    else -> "Error al eliminar usuario"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun generarReportePDF( fechaInicio: String? = null, fechaFin: String? = null ): Resource<ResponseBody> {
        return try {
            val response = adminService.generarReportePDF(fechaInicio, fechaFin)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error al generar reporte")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }

    suspend fun obtenerEstadisticasUsuario(usuarioId: Int): Resource<EstadisticasUsuario> {
        return try {
            val response = adminService.obtenerEstadisticasUsuario(usuarioId)

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