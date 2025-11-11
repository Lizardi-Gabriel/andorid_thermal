package com.thermal.monitoring.data.remote

import com.google.gson.annotations.SerializedName

// Enums
enum class RolUsuarioEnum {
    @SerializedName("admin") ADMIN,
    @SerializedName("operador") OPERADOR
}

enum class EstatusEventoEnum {
    @SerializedName("confirmado") CONFIRMADO,
    @SerializedName("descartado") DESCARTADO,
    @SerializedName("pendiente") PENDIENTE
}

enum class TipoMedicionEnum {
    @SerializedName("antes") ANTES,
    @SerializedName("durante") DURANTE,
    @SerializedName("despues") DESPUES,
    @SerializedName("pendiente") PENDIENTE
}

enum class TipoLogEnum {
    @SerializedName("info") INFO,
    @SerializedName("advertencia") ADVERTENCIA,
    @SerializedName("error") ERROR
}

// Request/Response para autenticacion
data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

// Usuario
data class Usuario(
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    val rol: RolUsuarioEnum
)

data class UsuarioCreate(
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    val password: String,
    val rol: RolUsuarioEnum = RolUsuarioEnum.OPERADOR
)

// Deteccion
data class Deteccion(
    @SerializedName("deteccion_id") val deteccionId: Int,
    @SerializedName("imagen_id") val imagenId: Int,
    val confianza: Float,
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int
)

// Imagen
data class Imagen(
    @SerializedName("imagen_id") val imagenId: Int,
    @SerializedName("evento_id") val eventoId: Int,
    @SerializedName("ruta_imagen") val rutaImagen: String,
    @SerializedName("hora_subida") val horaSubida: String,
    val detecciones: List<Deteccion> = emptyList()
)

// Calidad del Aire
data class CalidadAire(
    @SerializedName("registro_id") val registroId: Int,
    @SerializedName("evento_id") val eventoId: Int,
    @SerializedName("hora_medicion") val horaMedicion: String?,
    val temp: Float?,
    val humedad: Float?,
    @SerializedName("pm1p0") val pm1p0: Float?,
    @SerializedName("pm2p5") val pm2p5: Float?,
    val pm10: Float?,
    val aqi: Float?,
    val descrip: String?,
    val tipo: TipoMedicionEnum
)

// Evento
data class Evento(
    @SerializedName("evento_id") val eventoId: Int,
    @SerializedName("fecha_evento") val fechaEvento: String,
    val descripcion: String?,
    val estatus: EstatusEventoEnum,
    @SerializedName("usuario_id") val usuarioId: Int?,
    val usuario: Usuario?,
    val imagenes: List<Imagen> = emptyList(),
    @SerializedName("registros_calidad_aire") val registrosCalidadAire: List<CalidadAire> = emptyList()
)

data class EventoCreate(
    @SerializedName("fecha_evento") val fechaEvento: String,
    val descripcion: String? = null,
    val estatus: EstatusEventoEnum = EstatusEventoEnum.PENDIENTE
)



data class LogSistemaCreate(
    val tipo: TipoLogEnum = TipoLogEnum.INFO,
    val mensaje: String
)

// Respuestas genericas de error
data class ErrorResponse(
    val detail: String
)

// Token FCM
data class TokenFCMRequest(
    @SerializedName("token_fcm") val tokenFcm: String,
    val dispositivo: String?
)

data class TokenFCMResponse(
    @SerializedName("token_id") val tokenId: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("token_fcm") val tokenFcm: String,
    val dispositivo: String?,
    @SerializedName("fecha_registro") val fechaRegistro: String,
    val activo: Boolean
)



// Evento Optimizado (para lista)
data class EventoOptimizado(
    @SerializedName("evento_id") val eventoId: Int,
    @SerializedName("fecha_evento") val fechaEvento: String,
    val descripcion: String?,
    val estatus: EstatusEventoEnum,
    @SerializedName("usuario_id") val usuarioId: Int?,
    val usuario: Usuario?,

    // Campos calculados
    @SerializedName("total_imagenes") val totalImagenes: Int,
    @SerializedName("max_detecciones") val maxDetecciones: Int,
    @SerializedName("total_detecciones") val totalDetecciones: Int,
    @SerializedName("hora_inicio") val horaInicio: String?,
    @SerializedName("hora_fin") val horaFin: String?,

    // Promedios de calidad del aire
    @SerializedName("promedio_pm10") val promedioPm10: Float?,
    @SerializedName("promedio_pm2p5") val promedioPm2p5: Float?,
    @SerializedName("promedio_pm1p0") val promedioPm1p0: Float?,

    // Solo imagen preview
    @SerializedName("imagen_preview") val imagenPreview: Imagen?
)





// Evento Detalle Optimizado (para detalle con todas las imágenes)
data class EventoDetalleOptimizado(
    @SerializedName("evento_id") val eventoId: Int,
    @SerializedName("fecha_evento") val fechaEvento: String,
    val descripcion: String?,
    val estatus: EstatusEventoEnum,
    @SerializedName("usuario_id") val usuarioId: Int?,
    val usuario: Usuario?,

    // Campos calculados
    @SerializedName("total_imagenes") val totalImagenes: Int,
    @SerializedName("max_detecciones") val maxDetecciones: Int,
    @SerializedName("total_detecciones") val totalDetecciones: Int,
    @SerializedName("hora_inicio") val horaInicio: String?,
    @SerializedName("hora_fin") val horaFin: String?,

    // Promedios de calidad del aire
    @SerializedName("promedio_pm10") val promedioPm10: Float?,
    @SerializedName("promedio_pm2p5") val promedioPm2p5: Float?,
    @SerializedName("promedio_pm1p0") val promedioPm1p0: Float?,

    // Imagen preview
    @SerializedName("imagen_preview") val imagenPreview: Imagen?,

    // Todas las imagenes y registros (solo en detalle)
    val imagenes: List<Imagen> = emptyList(),
    @SerializedName("registros_calidad_aire") val registrosCalidadAire: List<CalidadAire> = emptyList()
)

// Estadisticas de eventos
data class EstadisticasEventos(
    @SerializedName("total_eventos") val totalEventos: Int,
    @SerializedName("eventos_pendientes") val eventosPendientes: Int,
    @SerializedName("eventos_confirmados") val eventosConfirmados: Int,
    @SerializedName("eventos_descartados") val eventosDescartados: Int,
    @SerializedName("total_detecciones") val totalDetecciones: Int,
    @SerializedName("promedio_detecciones_por_evento") val promedioDeteccionesPorEvento: Float,
    @SerializedName("fecha_inicio") val fechaInicio: String?,
    @SerializedName("fecha_fin") val fechaFin: String?
)







// Gestion de Usuarios (Admin)
data class UsuarioLista(
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    val rol: RolUsuarioEnum,
    @SerializedName("total_eventos_gestionados") val totalEventosGestionados: Int,
    @SerializedName("eventos_confirmados") val eventosConfirmados: Int,
    @SerializedName("eventos_descartados") val eventosDescartados: Int
)

data class UsuarioCreateRequest(
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    val password: String,
    val rol: RolUsuarioEnum
)

data class UsuarioUpdateRequest(
    @SerializedName("nombre_usuario") val nombreUsuario: String?,
    @SerializedName("correo_electronico") val correoElectronico: String?,
    val password: String?,
    val rol: RolUsuarioEnum?
)

data class EstadisticasUsuario(
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("total_eventos_gestionados") val totalEventosGestionados: Int,
    @SerializedName("eventos_confirmados") val eventosConfirmados: Int,
    @SerializedName("eventos_descartados") val eventosDescartados: Int
)








// Recuperacion de contraseña
data class SolicitudRecuperacionPassword(
    @SerializedName("correo_electronico") val correoElectronico: String
)

data class ValidarTokenResponse(
    val valido: Boolean,
    val mensaje: String
)

data class RestablecerPassword(
    val token: String,
    @SerializedName("nueva_password") val nuevaPassword: String
)

data class RestablecerPasswordResponse(
    val exito: Boolean,
    val mensaje: String
)



