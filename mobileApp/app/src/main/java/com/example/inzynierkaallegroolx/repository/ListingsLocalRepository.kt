package com.example.inzynierkaallegroolx.repository

import android.content.Context
import com.example.inzynierkaallegroolx.data.AppDatabase
import com.example.inzynierkaallegroolx.data.listings.ListingEntity
import com.example.inzynierkaallegroolx.ui.screens.ListingItemUi

class ListingsLocalRepository(context: Context) {
    private val db = AppDatabase.get(context)
    suspend fun all(): List<ListingItemUi> = db.listings().all().map { ListingItemUi(it.id, it.title, it.price) }
    suspend fun upsert(list: List<ListingItemUi>) = db.listings().upsertAll(list.map { ListingEntity(it.id, it.title, it.title, it.price, "ACTIVE") })
}
