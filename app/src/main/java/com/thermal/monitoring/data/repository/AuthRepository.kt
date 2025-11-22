package com.thermal.monitoring.data.repository

import com.thermal.monitoring.data.local.TokenManager
import com.thermal.monitoring.data.remote.AuthService
import com.thermal.monitoring.data.remote.TokenFCMRequest
import com.thermal.monitoring.data.remote.Usuario
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
                        userId = usuario.usuarioId,
                        correo = usuario.correoElectronico
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


    // Cerrar sesion
    suspend fun logout() {
        tokenManager.limpiarDatos()
    }

    suspend fun registrarTokenFCM(tokenFCM: String, dispositivo: String = "Android"): Resource<Unit> {
        return try {
            val tokenData = TokenFCMRequest(tokenFcm = tokenFCM, dispositivo = dispositivo)
            val response = authService.registrarTokenFCM(tokenData)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Error al registrar token FCM")
            }
        } catch (e: Exception) {
            Resource.Error("Error de conexion: ${e.localizedMessage}")
        }
    }


    suspend fun desactivarTokenFCM(): Resource<Unit> {
        return try {
            // 1 obtener el token actual del dispositivo
            // 2 llamar al endpoint de desactivación
            // implementacion actual: limpiamos los datos locales
            tokenManager.limpiarDatos()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error al desactivar token: ${e.localizedMessage}")
        }
    }



    // Verificar si hay sesion activa
    fun haySesionActiva() = tokenManager.haySesionActiva()


}