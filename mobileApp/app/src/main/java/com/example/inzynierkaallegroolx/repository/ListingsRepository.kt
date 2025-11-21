package com.example.inzynierkaallegroolx.repository

import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.ui.screens.ListingItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListingsRepository {
    suspend fun fetch(): Result<List<ListingItemUi>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val list = ApiClient.listings.getListings().map { ListingItemUi(it.id, it.title, it.price) }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }
}
