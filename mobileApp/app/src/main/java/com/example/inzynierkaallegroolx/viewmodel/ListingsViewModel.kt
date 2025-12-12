package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.repository.ListingsRepository
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOption {
    TITLE_ASC, TITLE_DESC, PRICE_ASC, PRICE_DESC
}

data class ListingsState(
    val listings: List<ListingItemUi> = emptyList(),
    val filteredListings: List<ListingItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterPlatform: String = "ALL",
    val isImporting: Boolean = false,
    val importMessage: String? = null,
    val sortOption: SortOption = SortOption.TITLE_ASC
)

class ListingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ListingsRepository(app)
    private val _state = MutableStateFlow(ListingsState())
    val state = _state.asStateFlow()

    init {
        loadListings()
    }

    fun loadListings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = repository.fetchAll()

            result.onSuccess { list ->
                _state.value = _state.value.copy(isLoading = false, listings = list)
                applyFilters(list)
            }.onFailure { e ->
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

    fun onSortChange(option: SortOption) {
        _state.value = _state.value.copy(sortOption = option)
        applyFilters(_state.value.listings)
    }

    fun importAllegro() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, error = null, importMessage = null)
            try {
                val result = repository.importFromAllegro()
                result.onSuccess { data ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        importMessage = "Zaimportowano: ${data.imported}, Zaktualizowano: ${data.updated}"
                    )
                    loadListings()
                }.onFailure { e ->
                    _state.value = _state.value.copy(isImporting = false, error = "Błąd importu: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isImporting = false, error = "Błąd: ${e.message}")
            }
        }
    }

    fun clearImportMessage() {
        _state.value = _state.value.copy(importMessage = null)
    }

    private fun applyFilters(currentListings: List<ListingItemUi>) {
        val s = _state.value
        val filtered = currentListings.filter { listing ->
            val matchesSearch = listing.title.contains(s.searchQuery, ignoreCase = true)
            val matchesPlatform = when (s.filterPlatform) {
                "ALLEGRO" -> listing.platforms.any { it.equals("ALLEGRO", ignoreCase = true) }
                "EBAY" -> listing.platforms.any { it.equals("EBAY", ignoreCase = true) }
                else -> true
            }
            matchesSearch && matchesPlatform
        }
        val sorted = when (s.sortOption) {
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOption.PRICE_ASC -> filtered.sortedBy { it.price.toDoubleOrNull() ?: 0.0 }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price.toDoubleOrNull() ?: 0.0 }
        }
        _state.value = s.copy(filteredListings = sorted)
    }

    fun deleteListing(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                repository.delete(id)
                loadListings()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nie udało się usunąć"
                )
            }
        }
    }
}