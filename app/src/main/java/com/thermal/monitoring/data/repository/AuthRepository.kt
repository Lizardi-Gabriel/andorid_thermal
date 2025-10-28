package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.data.remote.AuthService
import com.thermal.monitoring.data.remote.Usuario
import com.thermal.monitoring.data.remote.UsuarioCreate
import com.thermal.monitoring.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {

    // Realizar login
    suspend fun login(username: String, password: String): Resource<Usuario> {
        return try {
            val response = authService.login(username, password)

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!

                // Guardar token
                tokenManager.guardarToken(tokenResponse.accessToken)

                // Obtener datos del usuario actual
                val userResponse = authService.obtenerUsuarioActual()

                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val usuario = userResponse.body()!!

                    // Guardar datos del usuario
                    tokenManager.guardarDatosUsuario(
                        username = usuario.nombreUsuario,
                        role = usuario.rol.name,
                        userId = usuario.usuarioId
                    )

                    Resource.Success(usuario)
                } else {
                    Resource.Error("Error al obtener datos del usuario")
                }
            } else {
                Resource.Error("Usuario o contraseña incorrectos")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Crear nuevo usuario
    suspend fun crearUsuario(usuarioCreate: UsuarioCreate): Resource<Usuario> {
        return try {
            val response = authService.crearUsuario(usuarioCreate)

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
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Obtener usuario actual
    suspend fun obtenerUsuarioActual(): Resource<Usuario> {
        return try {
            val response = authService.obtenerUsuarioActual()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("No se pudo obtener el usuario")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    // Cerrar sesion
    suspend fun logout() {
        tokenManager.limpiarDatos()
    }

    // Verificar si hay sesion activa
    fun haySesionActiva() = tokenManager.haySesionActiva()
}