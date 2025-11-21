package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.ui.Routes
import com.example.inzynierkaallegroolx.viewmodel.ListingsViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun ListingsScreen(vm: ListingsViewModel = viewModel(), navController: NavController? = null) {
    val state by vm.state.collectAsState()
    val nav = navController ?: rememberNavController()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            Button(onClick = { nav.navigate(Routes.LISTING_EDIT) }) { Text("New Listing") }
            Button(onClick = { nav.navigate(Routes.MESSAGES) }) { Text("Messages") }
            Button(onClick = { nav.navigate(Routes.TEMPLATES) }) { Text("Templates") }
            Button(onClick = { nav.navigate(Routes.PROFILE) }) { Text("Profile") }
            Button(onClick = { nav.navigate(Routes.REPORTS) }) { Text("Reports") }
            if (state.items.firstOrNull() != null) Button(onClick = { nav.navigate(Routes.STATS + "/" + state.items.first().id) }) { Text("Stats") }
        }
        if (state.loading) CircularProgressIndicator() else ListingsList(state.items)
    }
}

@Composable
fun ListingsList(items: List<ListingItemUi>) {
    LazyColumn { items(items) { Text(it.title + " - " + it.price) } }
}

data class ListingItemUi(val id: String, val title: String, val price: String)
