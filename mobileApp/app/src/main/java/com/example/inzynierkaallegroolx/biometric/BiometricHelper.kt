package com.example.inzynierkaallegroolx.biometric

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val prompt = BiometricPrompt(activity, activity.mainExecutor, object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { onError(errString.toString()) }
            override fun onAuthenticationFailed() { }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }
}
