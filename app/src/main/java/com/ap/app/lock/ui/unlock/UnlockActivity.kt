package com.ap.app.lock.ui.unlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ap.app.lock.ui.MainActivity
import com.ap.app.lock.ui.onboarding.OnboardingActivity
import com.ap.app.lock.ui.theme.AppLockTheme
import com.ap.app.lock.ui.theme.ThemeRepository
import com.ap.app.lock.utils.BiometricAuthenticator
import com.ap.app.lock.utils.UnlockManager
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.launch

class UnlockActivity : AppCompatActivity() {

    private val viewModel: UnlockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT)
        }

        val packageName = intent.getStringExtra("package_name") ?: ""
        val appName = intent.getStringExtra("app_name") ?: ""
        val fromSplash = intent.getBooleanExtra("fromSplash", false)
        val allPermissionsGranted = intent.getBooleanExtra("allPermissionsGranted", true)
        val splashAuthMethod = intent.getStringExtra("appLockAuthMethod")

        // Initialize the ViewModel with intent extras
        viewModel.init(fromSplash, splashAuthMethod)

        lifecycleScope.launch {
            val themeSettings = ThemeRepository(this@UnlockActivity).getThemeSettings()

            setContent {
                val darkTheme = when (themeSettings.themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }
                AppLockTheme(darkTheme = darkTheme, dynamicColor = themeSettings.useDynamicColors) {
                    UnlockScreen(
                        viewModel = viewModel,
                        packageName = packageName,
                        appName = appName,
                        activity = this@UnlockActivity,
                        fromSplash = fromSplash,
                        onUnlockSuccess = {
                            UnlockManager.recordUnlock(packageName)
                            if (fromSplash) {
                                if (allPermissionsGranted) {
                                    val intent = Intent(this@UnlockActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(this@UnlockActivity, OnboardingActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            }
                            finish()
                        },
                        onCancel = {
                            startActivity(
                                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                            )
                            finish()
                        }
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back is disabled on unlock screen
            }
        })
    }
}

@Composable
fun UnlockScreen(
    viewModel: UnlockViewModel,
    packageName: String,
    appName: String,
    activity: AppCompatActivity,
    fromSplash: Boolean,
    onUnlockSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val isReady by viewModel.isReady.collectAsState()
    val activeUnlockMethod by viewModel.activeUnlockMethod.collectAsState()
    val appLockType by viewModel.appLockType.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var authMessage by remember { mutableStateOf("Authenticate to unlock") }

    // This state determines what UI to show. It starts with the method from the VM,
    // but can be changed to "pin" as a fallback.
    var currentUiMode by remember(activeUnlockMethod) { mutableStateOf(activeUnlockMethod) }

    val biometricAuthenticator = BiometricAuthenticator(
        activity = activity,
        onAuthenticationSuccess = onUnlockSuccess,
        onAuthenticationError = {
            authMessage = it
            // CRITICAL CHANGE: If biometrics fail, switch to PIN UI as a fallback
            if (activeUnlockMethod != "biometric") {
                currentUiMode = "pin"
            }
        }
    )

    val context = LocalContext.current
    val appIcon = try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: Exception) {
        null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (!isReady) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // App icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIcon != null) {
                            Image(
                                painter = rememberDrawablePainter(drawable = appIcon),
                                contentDescription = "App Icon",
                                modifier = Modifier.fillMaxSize(0.7f)
                            )
                        } else {
                            Text("🔒", fontSize = 40.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (fromSplash && currentUiMode != "pin") "Waiting for authentication" else authMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- Authentication UI --- //

                    if (currentUiMode == "biometric" || currentUiMode == "device_lock") {
                        CircularProgressIndicator()

                        LaunchedEffect(currentUiMode) {
                            biometricAuthenticator.authenticate(
                                allowDeviceCredential = currentUiMode == "device_lock"
                            )
                        }

                        if (!fromSplash) {
                            Spacer(modifier = Modifier.height(32.dp))
                            TextButton(onClick = { currentUiMode = "pin" }) {
                                Text("Use PIN Instead")
                            }
                        }

                    } else {
                        // PIN / Password entry
                        val isPassword = appLockType == "Password"
                        val inputLabel = if (isPassword) "Enter Password" else "Enter PIN"
                        val errorLabel = if (isPassword) "Incorrect Password" else "Incorrect PIN"

                        if (activeUnlockMethod != "biometric") {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text(inputLabel) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (isPassword) KeyboardType.Password
                                    else KeyboardType.NumberPassword
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                singleLine = true,
                                isError = authMessage != "Authenticate to unlock"
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (viewModel.verifyPassword(pinInput)) {
                                        onUnlockSuccess()
                                    } else {
                                        authMessage = errorLabel
                                        pinInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.8f),
                                enabled = pinInput.isNotEmpty()
                            ) {
                                Text("Unlock")
                            }
                        }

                        if (!fromSplash && (activeUnlockMethod == "biometric" || activeUnlockMethod == "device_lock")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { currentUiMode = activeUnlockMethod }) {
                                Text("Use Biometric Instead")
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                }
            }
        }
    }
}