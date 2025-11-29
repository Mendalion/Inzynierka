package com.example.inzynierkaallegroolx.util

inline fun <T> Result<T>.onFailureMessage(default: String = "Error"): String = exceptionOrNull()?.message ?: default
