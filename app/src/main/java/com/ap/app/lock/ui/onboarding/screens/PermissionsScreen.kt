package com.ap.app.lock.ui.onboarding.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ap.app.lock.utils.PermissionHelper
import kotlinx.coroutines.delay

@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit,
    onRequestDeviceAdmin: () -> Unit      // ← Activity-level launcher, passed in from OnboardingActivity
) {
    val context = LocalContext.current

    var permissionStates by remember {
        mutableStateOf(getCurrentPermissions(context))
    }

    // Poll every 500ms — detects changes after user returns from any system settings screen
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            permissionStates = getCurrentPermissions(context)
        }
    }

    // Auto-advance as soon as every permission is granted
    LaunchedEffect(permissionStates) {
        if (permissionStates.all { it.value }) {
            onPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Required Permissions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Please grant all permissions for the app to work properly",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            permissionStates.forEach { (key, isGranted) ->
                PermissionItem(
                    permission = key,
                    isGranted = isGranted,
                    onPermissionRequest = {
                        when (key) {
                            "notifications" -> PermissionHelper.requestNotificationPermission(context)
                            "overlay"       -> PermissionHelper.requestOverlayPermission(context)
                            "usage_stats"   -> PermissionHelper.requestUsageStatsPermission(context)
                            "accessibility" -> PermissionHelper.requestAccessibilityPermission(context)
                            "battery"       -> PermissionHelper.requestBatteryOptimizationPermission(context)

                            // Device Admin: call the Activity-level launcher — NOT context.startActivity()
                            // This is the fix. Using context.startActivity() for ACTION_ADD_DEVICE_ADMIN
                            // causes the dialog to open but the result (granted/denied) is lost,
                            // making it appear as if the permission was never granted.
                            "admin" -> onRequestDeviceAdmin()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Fallback manual continue button — only enabled when all permissions are granted
        Button(
            onClick = onPermissionsGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = permissionStates.all { it.value }
        ) {
            Text(
                text = if (permissionStates.all { it.value }) "Continue" else "Grant All Permissions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getCurrentPermissions(context: Context): Map<String, Boolean> {
    return mapOf(
        "notifications" to PermissionHelper.hasNotificationPermission(context),
        "overlay"       to PermissionHelper.hasOverlayPermission(context),
        "usage_stats"   to PermissionHelper.hasUsageStatsPermission(context),
        "accessibility" to PermissionHelper.hasAccessibilityPermission(context),
        "admin"         to PermissionHelper.isDeviceAdminEnabled(context),
        "battery"       to PermissionHelper.isBatteryOptimizationIgnored(context)
    )
}

@Composable
fun PermissionItem(
    permission: String,
    isGranted: Boolean,
    onPermissionRequest: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isGranted)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        label = "permission_bg"
    )

    val (icon, title, description) = when (permission) {
        "notifications" -> Triple("🔔", "Notifications",        "Show app lock notifications")
        "overlay"       -> Triple("📱", "Overlay",              "Display unlock screen over apps")
        "usage_stats"   -> Triple("📊", "Usage Stats",          "Monitor app launches")
        "accessibility" -> Triple("♿", "Accessibility",         "Advanced app monitoring")
        "admin"         -> Triple("🔐", "Device Admin",         "Lock device & prevent uninstall")
        "battery"       -> Triple("🔋", "Battery Optimization", "Keep protection running in background")
        else            -> Triple("❓", "Unknown",              "Unknown permission")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onPermissionRequest,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Grant", fontSize = 10.sp)
                }
            }
        }
    }
}