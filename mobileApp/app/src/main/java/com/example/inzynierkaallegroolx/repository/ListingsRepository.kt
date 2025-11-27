package com.example.inzynierkaallegroolx.repository

import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListingsRepository {
    suspend fun fetch(): Result<List<ListingItemUi>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resultDto = ApiClient.listings.getListings()

            //parametry
            val list = resultDto.map { dto ->
                ListingItemUi(
                    id = dto.id,
                    title = dto.title,
                    price = dto.price ?: "0.00",
                    status = dto.status ?: "UNKNOWN",
                    platform = dto.platform ?: "OTHER"
                )
            }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }
}