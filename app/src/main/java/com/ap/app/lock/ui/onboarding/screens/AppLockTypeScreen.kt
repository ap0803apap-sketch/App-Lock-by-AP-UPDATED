package com.ap.app.lock.ui.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ap.app.lock.ui.onboarding.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockTypeScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val lockTypes = listOf("4 Digit Pin", "6 Digit Pin", "Unlimited Digit Pin", "Password")
    val selectedLockType by viewModel.appLockType.collectAsState()
    val isChangePasscodeMode by viewModel.isChangePasscodeMode.collectAsState()

    var pinValue by remember { mutableStateOf("") }
    var confirmPinValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isPin = selectedLockType.contains("Pin")

    fun validate(): Boolean {
        errorMessage = null
        when (selectedLockType) {
            "4 Digit Pin" -> {
                if (pinValue.length != 4 || !pinValue.all { it.isDigit() }) {
                    errorMessage = "PIN must be 4 digits."
                    return false
                }
                if (pinValue != confirmPinValue) {
                    errorMessage = "PINs do not match."
                    return false
                }
            }
            "6 Digit Pin" -> {
                if (pinValue.length != 6 || !pinValue.all { it.isDigit() }) {
                    errorMessage = "PIN must be 6 digits."
                    return false
                }
                if (pinValue != confirmPinValue) {
                    errorMessage = "PINs do not match."
                    return false
                }
            }
            "Unlimited Digit Pin" -> {
                if (pinValue.isEmpty() || !pinValue.all { it.isDigit() }) {
                    errorMessage = "PIN must not be empty and contain only digits."
                    return false
                }
                if (pinValue != confirmPinValue) {
                    errorMessage = "PINs do not match."
                    return false
                }
            }
            "Password" -> {
                if (passwordValue.length < 8) {
                    errorMessage = "Password must be at least 8 characters."
                    return false
                }
                if (!passwordValue.any { it.isDigit() }) {
                    errorMessage = "Password must contain at least one digit."
                    return false
                }
                if (!passwordValue.any { it.isLowerCase() }) {
                    errorMessage = "Password must contain at least one lowercase letter."
                    return false
                }
                if (!passwordValue.any { it.isUpperCase() }) {
                    errorMessage = "Password must contain at least one uppercase letter."
                    return false
                }
                if (passwordValue.all { it.isLetterOrDigit() }) {
                    errorMessage = "Password must contain at least one special character."
                    return false
                }
                if (passwordValue != confirmPasswordValue) {
                    errorMessage = "Passwords do not match."
                    return false
                }
            }
        }
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isChangePasscodeMode) "Change App Lock" else "Set App Lock",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isChangePasscodeMode) "Update the lock for the apps you want to protect." else "Set a lock for the apps you want to protect.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedLockType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Lock Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                lockTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            viewModel.setAppLockType(type)
                            expanded = false
                            // Reset fields when type changes
                            pinValue = ""
                            confirmPinValue = ""
                            passwordValue = ""
                            confirmPasswordValue = ""
                            errorMessage = null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isPin) {
            OutlinedTextField(
                value = pinValue,
                onValueChange = {
                    val newPin = it.take(if (selectedLockType == "4 Digit Pin") 4 else if (selectedLockType == "6 Digit Pin") 6 else Int.MAX_VALUE)
                    if (newPin.all { char -> char.isDigit() }) {
                        pinValue = newPin
                    }
                },
                label = { Text("Enter PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPinValue,
                onValueChange = {
                    val newPin = it.take(if (selectedLockType == "4 Digit Pin") 4 else if (selectedLockType == "6 Digit Pin") 6 else Int.MAX_VALUE)
                     if (newPin.all { char -> char.isDigit() }) {
                        confirmPinValue = newPin
                    }
                },
                label = { Text("Confirm PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else { // Password
            OutlinedTextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = { Text("Enter Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPasswordValue,
                onValueChange = { confirmPasswordValue = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                if (validate()) {
                    val valueToSave = if (isPin) pinValue else passwordValue
                    viewModel.setAppLockTypeValue(valueToSave)
                    onNext()
                }
            }) {
                Text(if (isChangePasscodeMode) "Save" else "Next")
            }
        }
    }
}