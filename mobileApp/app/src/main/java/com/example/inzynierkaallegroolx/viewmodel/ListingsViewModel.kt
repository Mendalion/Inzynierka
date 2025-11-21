package com.example.inzynierkaallegroolx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.repository.ListingsRepository
import com.example.inzynierkaallegroolx.ui.screens.ListingItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ListingsState(val loading: Boolean = true, val items: List<ListingItemUi> = emptyList(), val error: String? = null)

class ListingsViewModel: ViewModel() {
    private val repo = ListingsRepository()
    private val _state = MutableStateFlow(ListingsState())
    val state: StateFlow<ListingsState> = _state

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            val result = repo.fetch()
            _state.value = if (result.isSuccess) ListingsState(loading = false, items = result.getOrNull()!!) else ListingsState(loading = false, error = result.exceptionOrNull()?.message)
        }
    }
}
