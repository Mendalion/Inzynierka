package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.repository.ListingsRepository
import com.example.inzynierkaallegroolx.ui.model.ListingImageUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListingEditState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val attributes: List<Pair<String, String>> = emptyList(),
    val serverImages: List<ListingImageUi> = emptyList(),
    val newLocalImages: List<Uri> = emptyList(),
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class ListingEditViewModel(app: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(app) {

    private val repository = ListingsRepository(app)

    private val listingId: String = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow(ListingEditState())
    val state = _state.asStateFlow()

    init {
        loadListing(listingId)
    }

    fun loadListing(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.fetchDetails(id)
            result.onSuccess { item ->
                _state.value = ListingEditState(
                    id = item.id,
                    title = item.title,
                    description = item.description,
                    price = item.price,
                    categoryId = item.categoryId ?: "",
                    categoryName = item.categoryName ?: "Brak nazwy (ID: ${item.categoryId})",
                    attributes = item.attributes.toList(),
                    serverImages = item.allImages,
                    isLoading = false
                )
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd: ${it.message}")
            }
        }
    }

    fun onTitleChange(v: String) { _state.value = _state.value.copy(title = v) }
    fun onDescChange(v: String) { _state.value = _state.value.copy(description = v) }
    fun onCategoryChange(v: String) { _state.value = _state.value.copy(categoryId = v) }
    fun onPriceChange(v: String) {

        if (v.all { it.isDigit() || it == '.' || it == ',' }) {
            _state.value = _state.value.copy(price = v)
        }
    }

    fun addPhotos(uris: List<Uri>) {
        val current = _state.value.newLocalImages.toMutableList()
        current.addAll(uris)
        _state.value = _state.value.copy(newLocalImages = current)
    }

    fun onAttributeChange(index: Int, newVal: String) {
        val current = _state.value.attributes.toMutableList()
        val oldPair = current[index]
        current[index] = oldPair.copy(second = newVal)
        _state.value = _state.value.copy(attributes = current)
    }

    //usuwanie nowych zdjęć lokalnych zanim zostaną wysłane
    fun removeNewPhoto(uri: Uri) {
        val current = _state.value.newLocalImages.toMutableList()
        current.remove(uri)
        _state.value = _state.value.copy(newLocalImages = current)
    }

    fun deleteServerPhoto(image: ListingImageUi) {
        viewModelScope.launch {
            try {
                repository.deleteImage(_state.value.id, image.id)
                loadListing(_state.value.id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Nie udało się usunąć: ${e.message}")
            }
        }
    }

    fun saveChanges() {
        val s = _state.value
        val priceDouble = s.price.replace(',', '.').toDoubleOrNull()
        if (s.title.isBlank() || priceDouble == null) {
            _state.value = s.copy(error = "Tytuł i poprawna cena są wymagane")
            return
        }
        val attrsMap = s.attributes.toMap()

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            try {
                repository.update(
                    s.id,
                    s.title,
                    s.description,
                    priceDouble,
                    attrsMap,
                    s.serverImages,
                    s.newLocalImages
                )
                _state.value = s.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = s.copy(isLoading = false, error = "Błąd zapisu: ${e.message}")
            }
        }
    }
}