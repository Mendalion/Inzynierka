package com.example.inzynierkaallegroolx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inzynierkaallegroolx.biometric.BiometricHelper
import com.example.inzynierkaallegroolx.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import android.content.Context
import android.content.ContextWrapper

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, vm: AuthViewModel = viewModel()) {
    var isRegistering by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf<String?>(null) }

    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    //próba automatycznego odświeżenia sesji przy wejściu
    LaunchedEffect(Unit) { vm.refresh() }

    //przekierowanie po udanym logowaniu
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    // Helper: unwrap Context to find hosting FragmentActivity
    fun Context.findFragmentActivity(): FragmentActivity? {
        var current: Context? = this
        while (current is ContextWrapper) {
            if (current is FragmentActivity) return current
            current = current.baseContext
        }
        return null
    }

    // Determine if biometric auth is available on this device
    val canUseBiometric by remember { mutableStateOf(BiometricHelper.isBiometricAvailable(ctx)) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRegistering) "Utwórz konto" else "Witaj ponownie",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            //pole Imię (rejestracja)
            AnimatedVisibility(visible = isRegistering) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Imię (opcjonalne)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )
            }

            //pole Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            //pole Hasło
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Pokaż hasło"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            //glówny przycisk akcji
            Button(
                onClick = {
                    if (isRegistering) {
                        vm.register(email, password, name)
                    } else {
                        vm.login(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.loading
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isRegistering) "Zarejestruj się" else "Zaloguj się")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //przycisk Biometrii(logowaniu)
            if (!isRegistering && canUseBiometric) {
                OutlinedButton(
                    onClick = {
                        biometricError = null
                        // Prefer view.context which is tied to the real host Activity in Compose
                        val act = view.context.findFragmentActivity() ?: ctx.findFragmentActivity()
                        if (act != null) {
                            BiometricHelper.authenticate(
                                act,
                                onSuccess = { scope.launch { vm.biometricLogin() } },
                                onError = { msg -> biometricError = msg }
                            )
                        } else {
                            biometricError = "Brak wsparcia biometrii w tym widoku."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.loading
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Zaloguj odciskiem palca")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Show biometric-specific errors (separately from auth state errors)
            if (biometricError != null) {
                Text(
                    text = biometricError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            //przełącznik trybu
            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(
                    text = if (isRegistering) "Masz już konto? Zaloguj się" else "Nie masz konta? Zarejestruj się"
                )
            }

            //obsługa błędów
            if (state.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}