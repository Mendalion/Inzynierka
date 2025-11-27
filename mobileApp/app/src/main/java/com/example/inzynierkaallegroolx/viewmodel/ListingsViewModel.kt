package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ListingsState(
    val listings: List<ListingItemUi> = emptyList(),
    val filteredListings: List<ListingItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterPlatform: String = "ALL"
)

class ListingsViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ListingsState())
    val state = _state.asStateFlow()

    init {
        loadListings()
    }

    fun loadListings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val resultDto = ApiClient.listings.getListings()

                // Mapowanie DTO -> UI. Obsługa nulli za pomocą operatora ?:
                val resultUi = resultDto.map {
                    ListingItemUi(
                        id = it.id,
                        title = it.title,
                        price = it.price ?: "0.00",
                        status = it.status ?: "UNKNOWN",
                        platform = it.platform ?: "OTHER"
                    )
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    listings = resultUi,
                    filteredListings = resultUi
                )
                // Od razu aplikujemy filtry, żeby lista była zgodna z obecnym stanem filtrów
                applyFilters(resultUi)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Błąd: ${e.message}")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters(_state.value.listings)
    }

    fun onFilterChange(platform: String) {
        _state.value = _state.value.copy(filterPlatform = platform)
        applyFilters(_state.value.listings)
    }

    private fun applyFilters(currentListings: List<ListingItemUi>) {
        val s = _state.value
        val filtered = currentListings.filter { listing ->
            val matchesSearch = listing.title.contains(s.searchQuery, ignoreCase = true)
            val matchesPlatform = when (s.filterPlatform) {
                "ALLEGRO" -> listing.platform.equals("ALLEGRO", ignoreCase = true)
                "OLX" -> listing.platform.equals("OLX", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesPlatform
        }
        _state.value = s.copy(filteredListings = filtered)
    }

    fun deleteListing(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                ApiClient.listings.delete(id)
                loadListings()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Nie udało się usunąć")
            }
        }
    }
}