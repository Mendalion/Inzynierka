package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.inzynierkaallegroolx.biometric.BiometricHelper
import com.example.inzynierkaallegroolx.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, vm: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.padding(16.dp)) {
        Text("Login")
        OutlinedTextField(email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.login(email, password) }, enabled = !state.loading) { Text("Log in") }
        Button(onClick = {
            val act = ctx as? FragmentActivity ?: return@Button
            BiometricHelper.authenticate(act, onSuccess = {
                scope.launch { vm.biometricLogin("demoUserId") }
            }, onError = {})
        }) { Text("Biometric") }

        if (state.loggedIn) onLoggedIn()
        if (state.error != null) Text("Error: ${state.error}")
    }
}
