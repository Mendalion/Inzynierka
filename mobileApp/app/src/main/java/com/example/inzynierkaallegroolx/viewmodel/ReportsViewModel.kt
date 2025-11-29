package com.example.inzynierkaallegroolx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ReportCreateBody
import com.example.inzynierkaallegroolx.network.ReportDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReportsState(
    val current: ReportDto? = null,
    val error: String? = null
)

class ReportsViewModel: ViewModel() {
    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state
    private var lastId: String? = null

    fun create(type: String, start: String, end: String) {
        viewModelScope.launch {
            try {
                val r = ApiClient.reports.create(ReportCreateBody(type, start, end))
                lastId = r.id
                _state.value = ReportsState(current = r)
            } catch (e: Exception) { _state.value = ReportsState(error = e.message) }
        }
    }
    fun refreshStatus() {
        val id = lastId ?: return
        viewModelScope.launch {
            try {
                val r = ApiClient.reports.status(id)
                _state.value = _state.value.copy(current = r)
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }
}
