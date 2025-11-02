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

}

interface EventoService {
    // Obtener lista de eventos
    @GET("eventos")
    suspend fun listarEventos(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 25
    ): Response<List<Evento>>

    // Obtener eventos por fecha
    @GET("eventos/fecha/{fecha_evento}")
    suspend fun listarEventosPorFecha(
        @Path("fecha_evento") fechaEvento: String
    ): Response<List<Evento>>

    // Obtener detalle de un evento
    @GET("eventos/{evento_id}")
    suspend fun obtenerEvento(
        @Path("evento_id") eventoId: Int
    ): Response<Evento>

    // Actualizar estatus de un evento
    @PUT("eventos/{evento_id}/status")
    suspend fun actualizarEstatusEvento(
        @Path("evento_id") eventoId: Int,
        @Query("estatus") estatus: EstatusEventoEnum
    ): Response<Evento>

    // Crear nuevo evento
    @POST("eventos")
    suspend fun crearEvento(
        @Body evento: EventoCreate
    ): Response<Evento>
}

interface LogService {
    // Obtener logs con filtros opcionales
    @GET("logs")
    suspend fun listarLogs(
        @Query("fecha") fecha: String? = null,
        @Query("tipo") tipo: TipoLogEnum? = null
    ): Response<List<LogSistema>>

    // Crear nuevo log
    @POST("logs")
    suspend fun crearLog(
        @Body log: LogSistemaCreate
    ): Response<LogSistema>
}