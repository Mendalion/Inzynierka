package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.CategoryParameterDto
import com.example.inzynierkaallegroolx.repository.ListingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListingAddState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "", // ID kategorii (np. "2")

    val selectedPlatform: String = "ALLEGRO",

    val selectedPhotos: List<Uri> = emptyList(),

    val dynamicFields: List<CategoryParameterDto> = emptyList(),
    val parameterValues: Map<String, String> = emptyMap(),
    val isLoadingParams: Boolean = false,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ListingAddViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ListingsRepository(app)
    private val _state = MutableStateFlow(ListingAddState())
    val state = _state.asStateFlow()

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescriptionChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onPriceChange(v: String) {
        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }
    fun onCategoryChange(v: String) { _state.value = _state.value.copy(category = v) }

//    fun toggleAllegro(checked: Boolean) { _state.value = _state.value.copy(platformAllegro = checked) }
//    fun toggleOlx(checked: Boolean) { _state.value = _state.value.copy(platformOlx = checked) }
    fun selectPlatform(platform: String) {
        _state.value = _state.value.copy(selectedPlatform = platform)
    }

    fun addPhotos(uris: List<Uri>) {
        val current = _state.value.selectedPhotos.toMutableList()
        current.addAll(uris)
        _state.value = _state.value.copy(selectedPhotos = current)
    }

    // Pobieranie parametrów
    fun loadParametersForCategory() {
        val catId = _state.value.category
        if (catId.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingParams = true, error = null)
            try {
                val params = repository.getCategoryParameters(catId)
                _state.value = _state.value.copy(
                    isLoadingParams = false,
                    dynamicFields = params,
                    parameterValues = emptyMap() // Reset wartości przy zmianie kategorii
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingParams = false, error = "Błąd pobierania parametrów: ${e.message}")
            }
        }
    }

    // Aktualizacja wartości parametru
    fun onParameterChange(paramId: String, value: String) {
        val currentMap = _state.value.parameterValues.toMutableMap()
        currentMap[paramId] = value
        _state.value = _state.value.copy(parameterValues = currentMap)
    }

    fun submitListing() {
        val s = _state.value
        if (s.title.isBlank() || s.price.isBlank()) {
            _state.value = s.copy(error = "Tytuł i cena są wymagane")
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
                repository.createListing(
                    title = s.title,
                    description = s.description,
                    price = priceDouble,
                    platform = s.selectedPlatform,
                    photos = s.selectedPhotos,
                    categoryId = s.category.ifBlank { "2" },
                    parameterValues = s.parameterValues
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