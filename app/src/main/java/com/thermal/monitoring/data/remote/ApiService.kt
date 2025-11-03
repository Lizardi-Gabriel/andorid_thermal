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