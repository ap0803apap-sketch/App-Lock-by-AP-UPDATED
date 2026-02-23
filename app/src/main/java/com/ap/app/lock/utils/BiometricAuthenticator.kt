package com.ap.app.lock.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricAuthenticator(
    private val activity: AppCompatActivity,
    private val onAuthenticationSuccess: () -> Unit,
    private val onAuthenticationError: (String) -> Unit
) {

    private val executor = ContextCompat.getMainExecutor(activity)

    private val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticationSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    onAuthenticationError(errString.toString())
                }
            }
        }
    )

    fun authenticate(allowDeviceCredential: Boolean = false) {
        val authenticators = if (allowDeviceCredential) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(authenticators)

        if (!allowDeviceCredential) {
            promptInfoBuilder.setNegativeButtonText("Cancel")
        }

        val promptInfo = promptInfoBuilder.build()

        biometricPrompt.authenticate(promptInfo)
    }
}