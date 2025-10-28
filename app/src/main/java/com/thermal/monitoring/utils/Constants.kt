package com.thermal.monitoring.utils

object Constants {
    // Validaciones
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_USERNAME_LENGTH = 50

    // Mensajes de error
    const val ERROR_CAMPO_VACIO = "Este campo no puede estar vacío"
    const val ERROR_PASSWORD_CORTA = "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
    const val ERROR_EMAIL_INVALIDO = "Correo electrónico inválido"
    const val ERROR_USERNAME_LARGO = "El nombre de usuario no puede exceder $MAX_USERNAME_LENGTH caracteres"

    // Mensajes de éxito
    const val EXITO_USUARIO_CREADO = "Usuario creado exitosamente"
    const val EXITO_LOGIN = "Inicio de sesión exitoso"
}