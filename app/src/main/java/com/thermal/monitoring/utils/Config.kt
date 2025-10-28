package com.thermal.monitoring.utils

object Config {
    // URL base de la API
    const val BASE_URL = "http://4.155.33.198:8000/"

    // Timeouts de red (en segundos)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Configuracion de logging
    const val ENABLE_LOGGING = true

    // Configuracion de autenticacion
    const val TOKEN_EXPIRATION_BUFFER_MINUTES = 5
}