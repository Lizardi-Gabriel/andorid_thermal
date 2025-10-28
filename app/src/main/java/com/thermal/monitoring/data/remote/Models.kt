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

// Log del Sistema
data class LogSistema(
    @SerializedName("log_id") val logId: Int,
    val tipo: TipoLogEnum,
    val mensaje: String,
    @SerializedName("hora_log") val horaLog: String
)

data class LogSistemaCreate(
    val tipo: TipoLogEnum = TipoLogEnum.INFO,
    val mensaje: String
)

// Respuestas genericas de error
data class ErrorResponse(
    val detail: String
)