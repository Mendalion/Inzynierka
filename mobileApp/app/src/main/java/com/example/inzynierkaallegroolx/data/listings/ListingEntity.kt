package com.example.inzynierkaallegroolx.data.listings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: String,
    val status: String
)
