package com.ap.app.lock.ui.onboarding.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(600))
        alpha.animateTo(1f, animationSpec = tween(600))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Animated Icon
        Icon(
            Icons.Default.Lock,
            contentDescription = "App Lock",
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Welcome Text
        Text(
            "Welcome to App Lock",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            "Secure your personal apps with biometric, PIN, or password protection. Your data is yours alone.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Features
        FeatureItem(
            icon = "🔒",
            title = "Complete Privacy",
            description = "Works completely offline. No data is collected or shared."
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureItem(
            icon = "⚡",
            title = "Fast & Reliable",
            description = "Lightning-fast unlock with biometric or PIN authentication."
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureItem(
            icon = "🛡️",
            title = "Tamper Proof",
            description = "Advanced protection against unauthorized access attempts."
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Next Button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeatureItem(icon: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}