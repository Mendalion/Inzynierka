package com.example.inzynierkaallegroolx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.repository.ListingsEditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ListingEditState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class ListingEditViewModel: ViewModel() {
    private val repo = ListingsEditRepository()
    private val _state = MutableStateFlow(ListingEditState())
    val state: StateFlow<ListingEditState> = _state

    fun create(title: String, description: String, price: Double) {
        _state.value = ListingEditState(loading = true)
        viewModelScope.launch {
            val result = repo.create(title, description, price)
            _state.value = if (result.isSuccess) ListingEditState(success = true) else ListingEditState(error = result.exceptionOrNull()?.message)
        }
    }
    fun update(id: String, title: String?, description: String?, price: Double?) {
        _state.value = ListingEditState(loading = true)
        viewModelScope.launch {
            val result = repo.update(id, title, description, price)
            _state.value = if (result.isSuccess) ListingEditState(success = true) else ListingEditState(error = result.exceptionOrNull()?.message)
        }
    }
}
