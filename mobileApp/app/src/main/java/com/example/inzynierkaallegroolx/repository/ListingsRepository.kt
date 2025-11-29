package com.example.inzynierkaallegroolx.repository

import com.example.inzynierkaallegroolx.Config
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListingsRepository {
    suspend fun fetch(): Result<List<ListingItemUi>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resultDto = ApiClient.listings.getListings()

            val list = resultDto.map { dto ->
                //mapowanie platform
                val platformsList = dto.platformStates?.map { it.platform } ?: emptyList()
                //mapowanie miniatury
                val thumb = Config.imageUrl(dto.images?.firstOrNull()?.url)
                ListingItemUi(
                    id = dto.id,
                    title = dto.title,
                    price = dto.price ?: "0.00",
                    status = dto.status ?: "UNKNOWN",
                    platforms = platformsList,
                    thumbnailUrl = thumb
                )
            }
            Result.success(list)
        } catch (e: Exception) { Result.failure(e) }
    }
}