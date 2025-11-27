package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingUpdateBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                    // ListingDto.price jest Stringiem (np. "123.00"), więc przypisujemy bezpośrednio
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
    // Walidacja wprowadzania (tylko cyfry, kropka, przecinek) - opcjonalnie
    fun onPriceChange(v: String) {
        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }

    fun saveChanges() {
        val s = _state.value

        val priceDouble = s.price.replace(',', '.').toDoubleOrNull()

        if (priceDouble == null) {
            _state.value = s.copy(error = "Niepoprawny format ceny")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            try {
                ApiClient.listings.update(
                    s.id,
                    ListingUpdateBody(
                        title = s.title,
                        description = s.description,
                        price = priceDouble // Przekazujemy Double
                    )
                )
                _state.value = s.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Błąd zapisu: ${e.message}")
            }
        }
    }
}