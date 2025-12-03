package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.data.auth.EncryptedTokenStore
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.UserUpdateBody
import com.example.inzynierkaallegroolx.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    //logowanie allegro
//    private val _showAllegroLogin = MutableStateFlow(false)
//    val showAllegroLogin: StateFlow<Boolean> = _showAllegroLogin
//
//w emulatrze nie renderuje porprawnie webview
//    private val _allegroAuthUrl = MutableStateFlow("")
//    val allegroAuthUrl: StateFlow<String> = _allegroAuthUrl
//    fun startAllegroAuth() {
//        viewModelScope.launch {
//            try {
//                _state.value = _state.value.copy(isLoading = true)
//                //pobieramy URL z backendu GET /integrations/oauth/allegro/start
//                val response = ApiClient.integration.startAuth("allegro")
//
//                _allegroAuthUrl.value = response.url
//                _showAllegroLogin.value = true
//            } catch (e: Exception) {
//                _state.value = _state.value.copy(error = "Błąd pobierania linku: ${e.message}")
//            } finally {
//                _state.value = _state.value.copy(isLoading = false)
//            }
//        }
//    }
//    fun finishAllegroAuth(code: String) {
//        _showAllegroLogin.value = false
//
//        viewModelScope.launch {
//            try {
//                _state.value = _state.value.copy(isLoading = true)
//
//                //wysyłamy kod do backendu POST /integrations/oauth/allegro/callback
//                    ApiClient.integration.sendCallback(
//                    "allegro",
//                    com.example.inzynierkaallegroolx.network.OAuthCallbackBody(code, "mobile_app")
//                )
//
//                _state.value = _state.value.copy(successMessage = "Pomyślnie połączono z Allegro!")
//            } catch (e: Exception) {
//                _state.value = _state.value.copy(error = "Błąd łączenia: ${e.message}")
//            } finally {
//                _state.value = _state.value.copy(isLoading = false)
//            }
//        }
//    }
//    fun dismissAllegroLogin() {
//        _showAllegroLogin.value = false
//    }
    fun startAllegroAuth(context: Context) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val response = com.example.inzynierkaallegroolx.network.ApiClient.integration.startAuth("allegro")

                //otwieramy w zewnetrznej przegladarce
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)

            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Błąd: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    fun finishAllegroAuth(code: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                //wysylamy skopiowany kod do serwera
                ApiClient.integration.sendCallback(
                    "allegro",
                    com.example.inzynierkaallegroolx.network.OAuthCallbackBody(code, "mobile_app")
                )

                _state.value = _state.value.copy(successMessage = "Pomyślnie połączono z Allegro!")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Błąd łączenia: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

}