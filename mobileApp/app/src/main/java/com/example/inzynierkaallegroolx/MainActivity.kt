package com.example.inzynierkaallegroolx

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.inzynierkaallegroolx.ui.AppNavigation

class MainActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@androidx.compose.runtime.Composable
fun AppRoot() {
    MaterialTheme {
        AppNavigation()
    }
}

@androidx.compose.ui.tooling.preview.Preview
@androidx.compose.runtime.Composable
fun PreviewApp() { AppRoot() }
