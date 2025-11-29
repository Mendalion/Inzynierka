package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.data.auth.EncryptedTokenStore
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.UserUpdateBody
import com.example.inzynierkaallegroolx.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val store = EncryptedTokenStore(app)
    private val authRepo = AuthRepository(store)

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val user = ApiClient.user.me()
                _state.value = _state.value.copy(
                    isLoading = false,
                    email = user.email,
                    name = user.name ?: "",
                    phone = user.phone ?: ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd pobierania profilu: ${e.message}")
            }
        }
    }

    fun updateProfile(newName: String, newPhone: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
                ApiClient.user.update(UserUpdateBody(name = newName, phone = newPhone))
                _state.value = _state.value.copy(
                    isLoading = false,
                    name = newName,
                    phone = newPhone,
                    successMessage = "Zapisano zmiany!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd zapisu: ${e.message}")
            }
        }
    }

    fun logout() {
        authRepo.logout()
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}