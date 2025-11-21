package com.example.inzynierkaallegroolx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ViewPoint
import com.example.inzynierkaallegroolx.network.SalePoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatsState(
    val views: List<ViewPoint>? = null,
    val sales: List<SalePoint>? = null,
    val error: String? = null
)

class StatsViewModel: ViewModel() {
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state

    fun load(listingId: String) {
        viewModelScope.launch {
            try {
                val v = ApiClient.stats.views(listingId)
                val s = ApiClient.stats.sales(listingId)
                _state.value = StatsState(v.views, s.sales)
            } catch (e: Exception) {
                _state.value = StatsState(error = e.message)
            }
        }
    }
}
