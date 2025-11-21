package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.network.ViewPoint
import com.example.inzynierkaallegroolx.network.SalePoint
import com.example.inzynierkaallegroolx.viewmodel.StatsViewModel

@Composable
fun SimpleViewsChart(points: List<ViewPoint>) {
    Column {
        val max = points.maxOfOrNull { it.count } ?: 1
        points.forEach { p ->
            Text(p.timestamp.take(10) + " |" + "#".repeat((20 * p.count / max).coerceAtLeast(1)))
        }
        if (points.isEmpty()) Text("No views data")
    }
}

@Composable
fun SimpleSalesChart(points: List<SalePoint>) {
    Column {
        val max = points.maxOfOrNull { it.count } ?: 1
        points.forEach { p -> Text(p.timestamp.take(10) + " |" + "*".repeat((20 * p.count / max).coerceAtLeast(1))) }
        if (points.isEmpty()) Text("No sales data")
    }
}

@Composable
fun StatsScreen(listingId: String) {
    val vm: StatsViewModel = viewModel()
    val state = vm.state.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { vm.load(listingId) }) { Text("Load Stats") }
        state.value.views?.let { SimpleViewsChart(it) }
        state.value.sales?.let { SimpleSalesChart(it) }
        state.value.error?.let { Text("Error: ${state.value.error}") }
    }
}
