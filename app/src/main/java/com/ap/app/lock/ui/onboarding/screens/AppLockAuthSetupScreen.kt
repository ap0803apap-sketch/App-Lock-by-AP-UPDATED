package com.ap.app.lock.ui.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ap.app.lock.ui.onboarding.OnboardingViewModel

@Composable
fun AppLockAuthSetupScreen(
    viewModel: OnboardingViewModel,
    onAuthenticationRequested: () -> Unit,
    onAuthenticationComplete: () -> Unit
) {
    val selectedAuthMethod by viewModel.appLockAuthMethod.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Secure Your App Lock",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Set authentication for App Lock itself",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAuthMethod == "biometric",
                    onClick = { viewModel.setAppLockAuthMethod("biometric") }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Biometric Only", fontWeight = FontWeight.Bold)
                    Text("Fingerprint or Face ID", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            // Device Lock Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAuthMethod == "device_lock",
                    onClick = { viewModel.setAppLockAuthMethod("device_lock") }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Device Lock", fontWeight = FontWeight.Bold)
                    Text("PIN/Password from device", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    "This lock protects the App Lock app itself and appears each time you open it.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp
                )
            }
        }

        Button(
            onClick = onAuthenticationRequested,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedAuthMethod.isNotEmpty()
        ) {
            Text("Authenticate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}