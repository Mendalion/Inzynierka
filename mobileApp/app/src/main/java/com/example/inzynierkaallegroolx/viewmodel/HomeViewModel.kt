package com.example.inzynierkaallegroolx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inzynierkaallegroolx.data.AppDatabase
import com.example.inzynierkaallegroolx.repository.ListingsLocalRepository
import com.example.inzynierkaallegroolx.repository.MessagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeStats(
    val activeListings: Int = 0,
    val archivedListings: Int = 0,
    val unreadMessages: Int = 0,
    val totalViews: Int = 0
)
//TODO logike xd
class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val _stats = MutableStateFlow(
        HomeStats(
            activeListings = 5,
            archivedListings = 2,
            unreadMessages = 9,
            totalViews = 1254
        )
    )

    val stats: StateFlow<HomeStats> = _stats.asStateFlow()
}