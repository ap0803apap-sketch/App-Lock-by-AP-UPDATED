package com.ap.app.lock.ui.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetectionMethodScreen(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Select Detection Method",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Choose how you want your apps to be detected",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Usage Stats Option
            MethodCard(
                title = "Usage Stats",
                description = "Monitors app usage to detect app launches",
                isSelected = selectedMethod == "usage_stats",
                onSelect = { onMethodSelected("usage_stats") },
                icon = "📊"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Accessibility Option
            MethodCard(
                title = "Accessibility Service",
                description = "Uses accessibility service for real-time detection",
                isSelected = selectedMethod == "accessibility",
                onSelect = { onMethodSelected("accessibility") },
                icon = "♿"
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedMethod.isNotEmpty()
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MethodCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    icon: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
