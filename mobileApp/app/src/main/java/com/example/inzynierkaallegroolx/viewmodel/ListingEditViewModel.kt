package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingUpdateBody // Musisz stworzyć to DTO w AuthApi/ListingApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Stan jest taki sam jak w dodawaniu
data class ListingEditState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ListingEditViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(ListingEditState())
    val state = _state.asStateFlow()

    fun loadListing(id: String) {
        viewModelScope.launch {
            try {
                val listing = ApiClient.listings.get(id)
                _state.value = ListingEditState(
                    id = listing.id,
                    title = listing.title,
                    description = listing.description ?: "",
                    price = listing.price ?: "",
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd pobierania: ${e.message}")
            }
        }
    }

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onPriceChange(v: String) { _state.value = _state.value.copy(price = v) }

    fun saveChanges() {
        val s = _state.value
        viewModelScope.launch {
            _state.value = s.copy(isLoading = true)
            try {
                ApiClient.listings.update(s.id, ListingUpdateBody(title = s.title, description = s.description, price = s.price))
                _state.value = s.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Błąd zapisu: ${e.message}")
            }
        }
    }
}