//package com.example.inzynierkaallegroolx.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.inzynierkaallegroolx.viewmodel.TemplatesViewModel
//
//@Composable
//fun TemplatesScreen() {
//    val vm: TemplatesViewModel = viewModel()
//    val state = vm.state.collectAsState()
//    var title by remember { mutableStateOf("") }
//    var body by remember { mutableStateOf("") }
//    Column(Modifier.fillMaxSize().padding(16.dp)) {
//        Row(Modifier.fillMaxWidth()) {
//            OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.weight(1f))
//            OutlinedTextField(body, { body = it }, label = { Text("Body") }, modifier = Modifier.weight(1f))
//        }
//        Button(onClick = { vm.create(title, body); title=""; body="" }, enabled = title.isNotBlank() && body.isNotBlank()) { Text("Add Template") }
//        LazyColumn(Modifier.weight(1f)) {
//            items(state.value.items) { t ->
//                Row(Modifier.fillMaxWidth().padding(4.dp)) {
//                    Text(t.title, Modifier.weight(1f))
//                    Button(onClick = { vm.delete(t.id) }) { Text("Del") }
//                }
//                Text(t.body)
//            }
//        }
//        state.value.error?.let { Text("Error: $it") }
//    }
//}
