package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingCreateBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListingAddState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val platformAllegro: Boolean = true,
    val platformOlx: Boolean = false,
    val photosCount: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ListingAddViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ListingAddState())
    val state = _state.asStateFlow()

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onCategoryChange(v: String) { _state.value = _state.value.copy(category = v) }
    fun onPriceChange(v: String) {
        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }

    fun toggleAllegro(checked: Boolean) { _state.value = _state.value.copy(platformAllegro = checked) }
    fun toggleOlx(checked: Boolean) { _state.value = _state.value.copy(platformOlx = checked) }

    fun addPhotoMock() {
        _state.value = _state.value.copy(photosCount = _state.value.photosCount + 1)
    }

    fun submitListing() {
        val s = _state.value
        if (s.title.isBlank() || s.price.isBlank()) {
            _state.value = s.copy(error = "Tytuł i cena są wymagane")
            return
        }

        if (!s.platformAllegro && !s.platformOlx) {
            _state.value = s.copy(error = "Wybierz przynajmniej jedną platformę")
            return
        }

        val priceDouble = s.price.replace(',', '.').toDoubleOrNull()
        if (priceDouble == null) {
            _state.value = s.copy(error = "Niepoprawny format ceny")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            try {
                ApiClient.listings.create(
                    ListingCreateBody(
                        title = s.title,
                        description = s.description,
                        price = priceDouble,
                        platform = if (s.platformAllegro) "ALLEGRO" else "OLX"
                    )
                )
                _state.value = s.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Błąd: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = ListingAddState()
    }
}