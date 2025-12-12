package com.example.inzynierkaallegroolx.ui.model

data class ListingImageUi(
    val id: String,
    val url: String
)

data class AllegroDetailsUi(
    val id: String,
    val status: String,
    val price: String,
    val stock: Int,
    val webUrl: String
)

data class ListingItemUi(
    val id: String,
    val title: String,
    val price: String,
    val status: String,
    val categoryId: String? = null,
    val platforms: List<String>,
    val thumbnailUrl: String? = null,
    val description: String = "",
    val allImages: List<ListingImageUi> = emptyList(),
    val allegroDetails: AllegroDetailsUi? = null
)