package com.example.inzynierkaallegroolx.ui.screens

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Files
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.network.ApiClient
import com.example.inzynierkaallegroolx.viewmodel.ReportsViewModel

@Composable
fun ReportsScreen() {
    val vm: ReportsViewModel = viewModel()
    val state = vm.state.collectAsState()
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("CSV") }
    var saveMsg by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start (ISO)") })
        OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End (ISO)") })
        OutlinedTextField(value = type, onValueChange = { type = it.uppercase() }, label = { Text("Type CSV/PDF") })
        Button(onClick = { vm.create(type, start, end); saveMsg = null }, enabled = start.isNotBlank() && end.isNotBlank()) { Text("Create Report") }
        state.value.current?.let { Text("Report ${it.id} status: ${it.status}") }
        Button(onClick = { vm.refreshStatus(); saveMsg = null }, enabled = state.value.current != null) { Text("Refresh Status") }
        state.value.error?.let { Text("Error: ${state.value.error}") }
        val current = state.value.current
        if (current?.status == "READY" && current.filePath != null) {
            Button(onClick = {
                val bytes = ApiClient.downloadReportFile(current.id)
                if (bytes != null) {
                    val resolver = ctx.contentResolver
                    val name = "report_${current.id}.${if (current.type == "CSV") "csv" else "pdf"}"
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, if (current.type == "CSV") "text/csv" else "application/pdf")
                    }
                    val targetUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    } else {
                        Files.getContentUri("external")
                    }
                    val uri = resolver.insert(targetUri, values)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { os -> os.write(bytes) }
                        saveMsg = "Saved as $name"
                    } else saveMsg = "Save failed"
                } else saveMsg = "Download failed"
            }) { Text("Save File") }
        }
        saveMsg?.let { Text(it) }
    }
}
