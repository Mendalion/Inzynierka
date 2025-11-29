// mobileApp/app/src/main/java/com/example/inzynierkaallegroolx/viewmodel/AuthViewModel.kt
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
import retrofit2.HttpException

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
            handleAuthResult(result)
        }
    }

    fun register(email: String, password: String, name: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = repo.register(email, password, name)
            handleAuthResult(result)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            //Ignorujemy błąd przy refreshu, po prostu nie loguje
            repo.refresh()
            if (store.access() != null) {
                _state.value = _state.value.copy(loggedIn = true, userId = store.userId())
            }
        }
    }

    fun biometricLogin() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            // Resolve userId if missing
            var userId = store.userId()
            if (userId == null) {
                // Try to fetch current user id via API using stored access token
                var fetched = runCatching { ApiClient.user.me().id }.getOrNull()
                if (fetched == null) {
                    // If unauthorized, try to refresh token and retry once
                    val refreshed = repo.refresh().isSuccess
                    if (refreshed) {
                        fetched = runCatching { ApiClient.user.me().id }.getOrNull()
                    }
                }
                if (fetched != null) {
                    store.saveAll(store.access() ?: "", store.refresh() ?: "", fetched)
                    userId = fetched
                }
            }

            if (userId == null) {
                _state.value = _state.value.copy(loading = false, error = "Zaloguj się najpierw hasłem, aby aktywować biometrię.")
                return@launch
            }

            val result = repo.biometric(userId)
            handleAuthResult(result)
        }
    }

    //Wspólna obsługa wyniku logowania/rejestracji
    private fun handleAuthResult(result: Result<Unit>) {
        if (result.isSuccess) {
            _state.value = AuthState(loading = false, loggedIn = true, userId = store.userId())
        } else {
            val msg = result.exceptionOrNull()?.message ?: "Błąd autoryzacji"
            _state.value = AuthState(loading = false, error = msg)
        }
    }
}