package com.example.inzynierkaallegroolx.repository

import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.ListingCreateBody
import com.example.inzynierkaallegroolx.network.ListingUpdateBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListingsEditRepository {
    suspend fun create(title: String, description: String, price: Double) = withContext(Dispatchers.IO) {
        runCatching { ApiClient.listings.create(ListingCreateBody(title, description, price)) }
    }
    suspend fun update(id: String, title: String?, description: String?, price: Double?) = withContext(Dispatchers.IO) {
        runCatching { ApiClient.listings.update(id, ListingUpdateBody(title, description, price)) }
    }
    suspend fun delete(id: String) = withContext(Dispatchers.IO) { runCatching { ApiClient.listings.delete(id) } }
    suspend fun archive(id: String) = withContext(Dispatchers.IO) { runCatching { ApiClient.listings.archive(id) } }
}
