package com.example.inzynierkaallegroolx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.inzynierkaallegroolx.ui.AppNavHost
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@Composable
fun AppRoot() {
    MaterialTheme {
        AppNavHost()
    }
}

@Preview
@Composable
fun PreviewApp() { AppRoot() }

