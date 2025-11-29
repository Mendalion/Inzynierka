package com.example.inzynierkaallegroolx

object Config {

    const val BASE_URL = "http://10.0.2.2:4000/"

    fun imageUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null
        if (path.startsWith("http")) return path
        return BASE_URL + path.removePrefix("/")
    }
}