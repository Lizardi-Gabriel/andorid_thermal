package com.thermal.monitoring.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermal.monitoring.data.remote.Usuario
import com.thermal.monitoring.data.repository.AuthRepository
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


    fun registrarTokenFCM(tokenFCM: String) {
        viewModelScope.launch {
            authRepository.registrarTokenFCM(tokenFCM)
        }
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