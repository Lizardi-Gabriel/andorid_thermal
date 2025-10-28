package com.thermal.monitoring.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.RolUsuarioEnum
import com.thermal.monitoring.data.remote.Usuario
import com.thermal.monitoring.data.remote.UsuarioCreate
import com.thermal.monitoring.data.repository.AuthRepository
import com.thermal.monitoring.utils.Constants
import com.thermal.monitoring.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // LiveData para el estado del login
    private val _loginState = MutableLiveData<Resource<Usuario>>()
    val loginState: LiveData<Resource<Usuario>> = _loginState

    // LiveData para el estado de registro
    private val _registroState = MutableLiveData<Resource<Usuario>>()
    val registroState: LiveData<Resource<Usuario>> = _registroState

    // LiveData para validaciones
    private val _validacionState = MutableLiveData<ValidationState>()
    val validacionState: LiveData<ValidationState> = _validacionState

    // Realizar login
    fun login(username: String, password: String) {
        // Validar campos
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = authRepository.login(username, password)
            _loginState.value = result
        }
    }

    // Crear nuevo usuario
    fun crearUsuario(
        nombreUsuario: String,
        correoElectronico: String,
        password: String,
        confirmarPassword: String,
        rol: RolUsuarioEnum = RolUsuarioEnum.OPERADOR
    ) {
        // Validar datos
        val validation = validarDatosRegistro(
            nombreUsuario,
            correoElectronico,
            password,
            confirmarPassword
        )

        if (!validation.isValid) {
            _validacionState.value = validation
            return
        }

        viewModelScope.launch {
            _registroState.value = Resource.Loading()
            val usuarioCreate = UsuarioCreate(
                nombreUsuario = nombreUsuario,
                correoElectronico = correoElectronico,
                password = password,
                rol = rol
            )
            val result = authRepository.crearUsuario(usuarioCreate)
            _registroState.value = result
        }
    }

    // Validar datos de registro
    private fun validarDatosRegistro(
        username: String,
        email: String,
        password: String,
        confirmarPassword: String
    ): ValidationState {
        return when {
            username.isBlank() -> ValidationState(
                false,
                usernameError = Constants.ERROR_CAMPO_VACIO
            )
            username.length > Constants.MAX_USERNAME_LENGTH -> ValidationState(
                false,
                usernameError = Constants.ERROR_USERNAME_LARGO
            )
            email.isBlank() -> ValidationState(
                false,
                emailError = Constants.ERROR_CAMPO_VACIO
            )
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationState(
                false,
                emailError = Constants.ERROR_EMAIL_INVALIDO
            )
            password.isBlank() -> ValidationState(
                false,
                passwordError = Constants.ERROR_CAMPO_VACIO
            )
            password.length < Constants.MIN_PASSWORD_LENGTH -> ValidationState(
                false,
                passwordError = Constants.ERROR_PASSWORD_CORTA
            )
            password != confirmarPassword -> ValidationState(
                false,
                confirmarPasswordError = "Las contraseñas no coinciden"
            )
            else -> ValidationState(true)
        }
    }

    // Limpiar estados
    fun limpiarEstados() {
        _loginState.value = null
        _registroState.value = null
        _validacionState.value = null
    }
}

// Data class para estado de validacion
data class ValidationState(
    val isValid: Boolean,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmarPasswordError: String? = null
)