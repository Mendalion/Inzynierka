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
        //Pobieramy ID z lokalnego storage (ostatni zalogowany użytkownik)
        val userId = store.userId()

        if (userId == null) {
            _state.value = _state.value.copy(error = "Zaloguj się najpierw hasłem, aby aktywować biometrię.")
            return
        }

        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
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