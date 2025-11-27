//package com.example.inzynierkaallegroolx.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.inzynierkaallegroolx.repository.MessagesRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//data class TemplatesState(
//    val items: List<com.example.inzynierkaallegroolx.data.messages.MessageTemplateEntity> = emptyList(),
//    val error: String? = null
//)
//
//class TemplatesViewModel(app: Application): AndroidViewModel(app) {
//    private val repo = MessagesRepository(app)
//    private val _state = MutableStateFlow(TemplatesState())
//    val state: StateFlow<TemplatesState> = _state
//
//    init { refresh() }
//
//    fun refresh() {
//        viewModelScope.launch {
//            val r = repo.syncTemplates()
//            val items = repo.templates()
//            _state.value = if (r.isSuccess) TemplatesState(items) else TemplatesState(error = r.exceptionOrNull()?.message)
//        }
//    }
//
//    fun create(title: String, body: String) {
//        viewModelScope.launch {
//            val r = repo.createTemplate(title, body)
//            if (r.isSuccess) refresh() else _state.value = _state.value.copy(error = r.exceptionOrNull()?.message)
//        }
//    }
//
//    fun delete(id: String) {
//        viewModelScope.launch {
//            val r = repo.deleteTemplate(id)
//            if (r.isSuccess) refresh() else _state.value = _state.value.copy(error = r.exceptionOrNull()?.message)
//        }
//    }
//}
