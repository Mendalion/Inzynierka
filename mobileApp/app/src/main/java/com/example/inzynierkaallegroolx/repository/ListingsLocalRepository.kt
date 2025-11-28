package com.example.inzynierkaallegroolx.repository

import android.content.Context
import com.example.inzynierkaallegroolx.data.AppDatabase
import com.example.inzynierkaallegroolx.data.listings.ListingEntity
import com.example.inzynierkaallegroolx.ui.model.ListingItemUi

class ListingsLocalRepository(context: Context) {
    // Upewnij się, że w AppDatabase masz metodę get lub getDatabase - użyj tej, która tam jest
    private val db = AppDatabase.getDatabase(context)

    suspend fun all(): List<ListingItemUi> = db.listingDao().getAll().map {
        ListingItemUi(
            id = it.id,
            title = it.title,
            price = it.price,
            status = it.status,
            platforms = emptyList(),
            thumbnailUrl = null
        )
    }

    suspend fun upsert(list: List<ListingItemUi>) {
        val entities = list.map {
            ListingEntity(
                id = it.id,
                title = it.title,
                description = "",
                price = it.price,
                status = it.status
            )
        }
        db.listingDao().upsertAll(entities)
    }
}