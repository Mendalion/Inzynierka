package com.example.inzynierkaallegroolx.ui.model

data class ListingItemUi(
    val id: String,
    val title: String,
    val price: String,
    val status: String,
    val platforms: List<String>,
    val thumbnailUrl: String? = null
)