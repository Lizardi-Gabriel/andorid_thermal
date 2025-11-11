package com.thermal.monitoring.data.remote

import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    // Endpoint publico para login
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    // Endpoint publico para crear usuario
    @POST("usuarios")
    suspend fun crearUsuario(
        @Body usuario: UsuarioCreate
    ): Response<Usuario>

    // Endpoint protegido para obtener usuario actual
    @GET("usuarios/me")
    suspend fun obtenerUsuarioActual(): Response<Usuario>

    // Endpoint para registrar token FCM
    @POST("registrar-token-fcm")
    suspend fun registrarTokenFCM(
        @Body tokenData: TokenFCMRequest
    ): Response<TokenFCMResponse>



    @POST("auth/forgot-password")
    suspend fun solicitarRecuperacionPassword(
        @Body solicitud: SolicitudRecuperacionPassword
    ): Response<Map<String, String>>

    @GET("auth/validate-reset-token/{token}")
    suspend fun validarTokenRecuperacion(
        @Path("token") token: String
    ): Response<ValidarTokenResponse>

    @POST("auth/reset-password")
    suspend fun restablecerPassword(
        @Body datos: RestablecerPassword
    ): Response<RestablecerPasswordResponse>



}

interface EventoService {
    @GET("eventos")
    suspend fun listarEventos(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 25
    ): Response<List<Evento>>

    @GET("eventos/fecha/{fecha_evento}")
    suspend fun listarEventosPorFecha(
        @Path("fecha_evento") fechaEvento: String
    ): Response<List<Evento>>

    @GET("eventos/{evento_id}")
    suspend fun obtenerEvento(
        @Path("evento_id") eventoId: Int
    ): Response<Evento>

    @PUT("eventos/{evento_id}/status")
    suspend fun actualizarEstatusEvento(
        @Path("evento_id") eventoId: Int,
        @Query("estatus") estatus: String? = null
    ): Response<Evento>

    @POST("eventos")
    suspend fun crearEvento(
        @Body evento: EventoCreate
    ): Response<Evento>

    // endpoints optimizados
    @GET("eventosfront/optimizado")
    suspend fun listarEventosOptimizado(
        @Query("estatus") estatus: String? = null,
        @Query("usuario_id") usuarioId: Int? = null,
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 1000
    ): Response<List<EventoOptimizado>>

    @GET("eventosfront/{evento_id}/optimizado")
    suspend fun obtenerEventoOptimizado(
        @Path("evento_id") eventoId: Int
    ): Response<EventoDetalleOptimizado>

    @GET("eventosfront/estadisticas")
    suspend fun obtenerEstadisticas(
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null
    ): Response<EstadisticasEventos>
}




interface AdminService {
    // Gestion de usuarios
    @GET("admin/usuarios")
    suspend fun listarUsuarios(): Response<List<UsuarioLista>>

    @POST("admin/usuarios")
    suspend fun crearUsuario(
        @Body usuario: UsuarioCreateRequest
    ): Response<Usuario>

    @PUT("admin/usuarios/{usuario_id}")
    suspend fun actualizarUsuario(
        @Path("usuario_id") usuarioId: Int,
        @Body usuario: UsuarioUpdateRequest
    ): Response<Usuario>

    @DELETE("admin/usuarios/{usuario_id}")
    suspend fun eliminarUsuario(
        @Path("usuario_id") usuarioId: Int
    ): Response<Unit>

    @GET("admin/usuarios/{usuario_id}")
    suspend fun obtenerUsuario(
        @Path("usuario_id") usuarioId: Int
    ): Response<Usuario>

    // Reportes PDF
    @GET("admin/reportes/generar-pdf")
    @Streaming
    suspend fun generarReportePDF(
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null
    ): Response<okhttp3.ResponseBody>

    @GET("estadisticas/{usuario_id}")
    suspend fun obtenerEstadisticasUsuario(
        @Path("usuario_id") usuarioId: Int
    ): Response<EstadisticasUsuario>

}


