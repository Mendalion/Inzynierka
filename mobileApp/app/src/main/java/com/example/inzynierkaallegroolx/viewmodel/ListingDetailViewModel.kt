package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.Config
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListingDetailState(
    val listing: ListingItemUi? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class ListingDetailViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val listingId: String? = savedStateHandle["id"]
    private val _state = MutableStateFlow(ListingDetailState())
    val state = _state.asStateFlow()

    init {
        loadListing()
    }

    fun loadListing() {
        val id = listingId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val dto = ApiClient.listings.get(id)

                val platformsList = dto.platformStates?.map { it.platform } ?: emptyList()
                val thumb = Config.imageUrl(dto.images?.firstOrNull()?.url)

                val uiModel = ListingItemUi(
                    id = dto.id,
                    title = dto.title,
                    price = dto.price ?: "0.00",
                    status = dto.status ?: "UNKNOWN",
                    platforms = platformsList,
                    thumbnailUrl = thumb
                )
                _state.value = _state.value.copy(isLoading = false, listing = uiModel)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Nie udało się pobrać: ${e.message}")
            }
        }
    }

    fun deleteListing() {
        val id = listingId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                ApiClient.listings.delete(id)
                _state.value = _state.value.copy(isLoading = false, isDeleted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd usuwania: ${e.message}")
            }
        }
    }
}