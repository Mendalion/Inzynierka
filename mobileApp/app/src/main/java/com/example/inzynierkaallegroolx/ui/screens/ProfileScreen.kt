package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.UserUpdateBody
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun ProfileScreen() {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        loading = true
        val me = runCatching { ApiClient.user.me() }.getOrNull()
        me?.let { name = it.name ?: ""; phone = it.phone ?: "" }
        loading = false
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text("Name") })
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone") })
        Button(onClick = {
            scope.launch {
                loading = true
                error = runCatching { ApiClient.user.update(UserUpdateBody(name.ifBlank { null }, phone.ifBlank { null })) }.exceptionOrNull()?.message
                loading = false
            }
        }, enabled = !loading) { Text("Save") }
        if (error != null) Text("Error: $error")
    }
}
