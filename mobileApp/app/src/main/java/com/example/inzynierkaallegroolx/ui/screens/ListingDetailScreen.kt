package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.network.IngestBody
import com.example.inzynierkaallegroolx.network.ListingUpdateBody
import kotlinx.coroutines.launch

@Composable
fun ListingDetailScreen(id: String) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    LaunchedEffect(id) {
        val l = runCatching { ApiClient.listings.get(id) }.getOrNull()
        l?.let { title = it.title; desc = it.description; price = it.price }
        runCatching { ApiClient.stats.ingestView(IngestBody(id)) }
    }
    Column(Modifier.padding(16.dp)) {
        Text("Listing detail $id")
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { scope.launch { runCatching { ApiClient.listings.update(id, ListingUpdateBody(title, desc, price.toDoubleOrNull())) } } }) { Text("Save") }
            Button(onClick = { scope.launch { runCatching { ApiClient.listings.archive(id) } } }) { Text("Archive") }
            Button(onClick = { scope.launch { runCatching { ApiClient.listings.delete(id) } } }) { Text("Delete") }
            Button(onClick = { scope.launch { runCatching { ApiClient.stats.ingestView(IngestBody(id)) } } }) { Text("Mark View") }
            Button(onClick = { scope.launch { runCatching { ApiClient.stats.ingestSale(IngestBody(id)) } } }) { Text("Mark Sale") }
        }
    }
}
