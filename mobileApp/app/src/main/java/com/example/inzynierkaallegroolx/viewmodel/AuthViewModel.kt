package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.data.auth.AuthState
import com.example.inzynierkaallegroolx.data.auth.EncryptedTokenStore
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application): AndroidViewModel(app) {
    private val store = EncryptedTokenStore(app)
    private val repo = AuthRepository(store)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init { ApiClient.setTokenProvider { store.access() } }

    fun login(email: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = repo.login(email, password)
            _state.value = if (result.isSuccess) AuthState(loading = false, loggedIn = true, userId = store.userId()) else AuthState(error = result.exceptionOrNull()?.message)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repo.refresh().onFailure { _state.value = _state.value.copy(error = it.message) }
            if (store.access()!=null) _state.value = _state.value.copy(loggedIn = true, userId = store.userId())
        }
    }

    fun biometricLogin(userId: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = repo.biometric(userId)
            _state.value = if (result.isSuccess) AuthState(loading = false, loggedIn = true, userId = store.userId()) else AuthState(error = result.exceptionOrNull()?.message)
        }
    }
}
