package com.thermal.monitoring.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val CORREO_KEY = stringPreferencesKey("correo")
    }

    // Guardar token y datos del usuario
    suspend fun guardarToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun guardarDatosUsuario(username: String, role: String, userId: Int, correo: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[ROLE_KEY] = role
            preferences[USER_ID_KEY] = userId.toString()
            preferences[CORREO_KEY] = correo
        }
    }

    // Obtener token
    fun obtenerToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    // Obtener username
    fun obtenerUsername(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }

    // Obtener rol
    fun obtenerRol(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ROLE_KEY]
        }
    }

    // Obtener correo
    fun obtenerCorreo(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[CORREO_KEY]
        }
    }

    // Obtener user ID
    fun obtenerUserId(): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]?.toIntOrNull()
        }
    }

    // Limpiar todos los datos (logout)
    suspend fun limpiarDatos() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Verificar si hay sesion activa
    fun haySesionActiva(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY] != null
        }
    }
}