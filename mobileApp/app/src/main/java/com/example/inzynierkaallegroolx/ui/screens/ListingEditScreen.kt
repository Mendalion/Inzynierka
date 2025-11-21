package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.viewmodel.ListingEditViewModel

@Composable
fun ListingEditScreen(onDone: () -> Unit, vm: ListingEditViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("0.0") }
    val state by vm.state.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("New Listing")
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(price, { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.create(title, description, price.toDoubleOrNull() ?: 0.0) }, enabled = !state.loading) { Text("Create") }
        if (state.success) onDone()
        state.error?.let { Text("Error: $it") }
    }
}
