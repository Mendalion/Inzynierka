package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Stan formularza
data class ListingAddState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: String = "",
    val platformAllegro: Boolean = true, // Domyślnie zaznaczone
    val platformOlx: Boolean = false,
    val photosCount: Int = 0, // Mock: liczba dodanych zdjęć
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ListingAddViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ListingAddState())
    val state = _state.asStateFlow()

    //Funkcje do aktualizacji pól
    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onCategoryChange(v: String) { _state.value = _state.value.copy(category = v) }
    fun onPriceChange(v: String) {
        // Pozwalamy wpisywać tylko cyfry i kropkę/przecinek (prosta walidacja UI)
        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }

    fun toggleAllegro(checked: Boolean) { _state.value = _state.value.copy(platformAllegro = checked) }
    fun toggleOlx(checked: Boolean) { _state.value = _state.value.copy(platformOlx = checked) }

    // Mock dodawania zdjęcia (tylko zwiększa licznik)
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

        //Symulacja wysyłania
        _state.value = s.copy(isLoading = true, error = null)

        //tutaj w przyszłości będzie strzał do API: ApiClient.listings.create(...)
        //Na razie udajemy sukces:
        _state.value = ListingAddState(isSuccess = true)
    }

    fun resetState() {
        _state.value = ListingAddState()
    }
}