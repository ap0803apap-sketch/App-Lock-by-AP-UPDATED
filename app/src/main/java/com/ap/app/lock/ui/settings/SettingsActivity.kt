package com.ap.app.lock.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ap.app.lock.R
import com.ap.app.lock.ui.onboarding.OnboardingActivity
import com.ap.app.lock.ui.theme.AppLockTheme

class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme by viewModel.theme.collectAsState()
            val useDynamicColors by viewModel.useDynamicColors.collectAsState()
            val darkTheme = when (theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            AppLockTheme(darkTheme = darkTheme, dynamicColor = useDynamicColors) {
                SettingsScreen(onBack = { finish() }, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val theme by viewModel.theme.collectAsState()
    val useDynamicColors by viewModel.useDynamicColors.collectAsState()
    val appLockAuthMethod by viewModel.appLockAuthMethod.collectAsState()
    val lockedAppUnlockMethod by viewModel.lockedAppUnlockMethod.collectAsState()
    val disableAdminSettings by viewModel.disableAdminSettings.collectAsState()
    val disableUsageStatsPage by viewModel.disableUsageStatsPage.collectAsState()
    val disableAccessibilityPage by viewModel.disableAccessibilityPage.collectAsState()
    val disableOverlayPage by viewModel.disableOverlayPage.collectAsState()
    val relockPolicy by viewModel.relockPolicy.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Appearance ──────────────────────────────────────────────────
            SettingsCard(title = "Appearance") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    SettingsOption(selected = theme == "system", onClick = { viewModel.setTheme("system") }, label = "System Default")
                    SettingsOption(selected = theme == "light", onClick = { viewModel.setTheme("light") }, label = "Light")
                    SettingsOption(selected = theme == "dark", onClick = { viewModel.setTheme("dark") }, label = "Dark")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dynamic Colors", fontSize = 14.sp)
                        Switch(
                            checked = useDynamicColors,
                            onCheckedChange = { viewModel.setUseDynamicColors(it) }
                        )
                    }
                }
            }

            // ── Relock Settings ─────────────────────────────────────────────
            SettingsCard(title = "Relock Timeout") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("How long to wait before locking apps again after you leave them:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsOption(selected = relockPolicy == "immediately", onClick = { viewModel.setRelockPolicy("immediately") }, label = "Immediately")
                    SettingsOption(selected = relockPolicy == "5s", onClick = { viewModel.setRelockPolicy("5s") }, label = "5 seconds after exit")
                    SettingsOption(selected = relockPolicy == "10s", onClick = { viewModel.setRelockPolicy("10s") }, label = "10 seconds after exit")
                    SettingsOption(selected = relockPolicy == "30s", onClick = { viewModel.setRelockPolicy("30s") }, label = "30 seconds after exit")
                    SettingsOption(selected = relockPolicy == "60s", onClick = { viewModel.setRelockPolicy("60s") }, label = "1 minute after exit")
                    SettingsOption(selected = relockPolicy == "screen_off", onClick = { viewModel.setRelockPolicy("screen_off") }, label = "When screen turns off")
                }
            }

            // ── Security ────────────────────────────────────────────────────
            SettingsCard(title = "Security") {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Change Passcode
                    Button(
                        onClick = {
                            val intent = Intent(context, OnboardingActivity::class.java).apply {
                                putExtra("isChangePasscodeMode", true)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Change Passcode / PIN")
                    }

                    HorizontalDivider()

                    // App Lock Security
                    Text("App Lock Security", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    SettingsOption(selected = appLockAuthMethod == "biometric", onClick = { viewModel.setAppLockAuthMethod("biometric") }, label = "Biometric Only")
                    SettingsOption(selected = appLockAuthMethod == "device_lock", onClick = { viewModel.setAppLockAuthMethod("device_lock") }, label = "Biometric + Device Lock")

                    HorizontalDivider()

                    // Locked App Security
                    Text("Locked App Security", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    SettingsOption(selected = lockedAppUnlockMethod == "both", onClick = { viewModel.setLockedAppUnlockMethod("both") }, label = "Biometric or Passcode")
                    SettingsOption(selected = lockedAppUnlockMethod == "biometric", onClick = { viewModel.setLockedAppUnlockMethod("biometric") }, label = "Biometric Only")
                    SettingsOption(selected = lockedAppUnlockMethod == "passcode", onClick = { viewModel.setLockedAppUnlockMethod("passcode") }, label = "Passcode Only")

                    HorizontalDivider()

                    SecuritySwitch(label = "Protect Admin Settings", checked = disableAdminSettings, onCheckedChange = { viewModel.setDisableAdminSettings(it) })
                    SecuritySwitch(label = "Protect Usage Stats", checked = disableUsageStatsPage, onCheckedChange = { viewModel.setDisableUsageStatsPage(it) })
                    SecuritySwitch(label = "Protect Accessibility", checked = disableAccessibilityPage, onCheckedChange = { viewModel.setDisableAccessibilityPage(it) })
                    SecuritySwitch(label = "Protect Overlay Settings", checked = disableOverlayPage, onCheckedChange = { viewModel.setDisableOverlayPage(it) })
                }
            }

            // ── About ─────────────────────────────────────────────
            SettingsCard(title = "About") {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text("App Lock by AP", fontWeight = FontWeight.Bold)

                    // EMAIL
                    TextButton(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("ap0803apap@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "App Lock by AP — Feedback")
                            }

                            context.startActivity(
                                Intent.createChooser(emailIntent, "Send email via…")
                            )
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = "Email",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text("ap0803apap@gmail.com", fontSize = 12.sp)
                        }
                    }

                    // GITHUB
                    TextButton(
                        onClick = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/ap0803apap-sketch")
                            )
                            context.startActivity(browserIntent)
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "GitHub",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text("GitHub Repository (Source code)", fontSize = 12.sp)
                        }
                    }

                    // COPYRIGHT (not clickable)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_law),
                            contentDescription = "Copyright",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "© 2026 App Lock by AP. All rights reserved",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsOption(selected: Boolean, onClick: () -> Unit, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, fontSize = 14.sp)
    }
}

@Composable
fun SecuritySwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
