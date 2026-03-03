package com.ap.app.lock.ui.unlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ap.app.lock.R
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

        // Security: Prevent screenshots and screen recording
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Set transparent status bar for immersive experience
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // Extract intent data
        val packageName = intent.getStringExtra("package_name") ?: ""
        val appName = intent.getStringExtra("app_name") ?: ""
        val fromSplash = intent.getBooleanExtra("fromSplash", false)
        val allPermissionsGranted = intent.getBooleanExtra("allPermissionsGranted", true)
        val splashAuthMethod = intent.getStringExtra("appLockAuthMethod")

        // Initialize ViewModel
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
                                val targetClass = if (allPermissionsGranted)
                                    MainActivity::class.java else OnboardingActivity::class.java

                                val intent = Intent(this@UnlockActivity, targetClass).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                startActivity(intent)
                            }
                            finish()
                        },
                        onCancel = {
                            UnlockManager.recordCancel(packageName)
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(homeIntent)
                            finish()
                        }
                    )
                }
            }
        }

        // Disable back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back button is disabled on unlock screen for security
            }
        })
    }

    override fun onResume() {
        super.onResume()
        UnlockManager.isLockShowing = true
    }

    override fun onPause() {
        super.onPause()
        // Keep lock showing during transitions
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't clear isLockShowing here - let the service manage it
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
    val lockedAppUnlockMethod by viewModel.lockedAppUnlockMethod.collectAsState()
    val appLockType by viewModel.appLockType.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var authMessage by remember { mutableStateOf("Authenticate to unlock") }
    var showPasswordVisibility by remember { mutableStateOf(false) }
    var currentUiMode by remember(activeUnlockMethod) { mutableStateOf(activeUnlockMethod) }

    val context = LocalContext.current
    val appIcon = try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (_: Exception) {
        null
    }

    val biometricAuthenticator = BiometricAuthenticator(
        activity = activity,
        onAuthenticationSuccess = onUnlockSuccess,
        onAuthenticationError = { errorMsg ->
            authMessage = errorMsg
            if (lockedAppUnlockMethod != "biometric" && activeUnlockMethod != "biometric") {
                currentUiMode = "pin"
            }
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (!isReady) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
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
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIcon != null) {
                            Image(
                                painter = rememberDrawablePainter(drawable = appIcon),
                                contentDescription = "App Icon",
                                modifier = Modifier.fillMaxSize(0.65f)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "App Lock",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        appName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        if (fromSplash && currentUiMode != "pin") "Waiting for authentication" else authMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    AnimatedContent(
                        targetState = currentUiMode,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "Auth mode transition"
                    ) { mode ->
                        when (mode) {
                            "biometric", "device_lock" -> {
                                BiometricAuthUI(
                                    biometricAuthenticator = biometricAuthenticator,
                                    currentUiMode = mode,
                                    lockedAppUnlockMethod = lockedAppUnlockMethod,
                                    fromSplash = fromSplash,
                                    onSwitchToPin = { currentUiMode = "pin" }
                                )
                            }
                            else -> {
                                PinAuthUI(
                                    pinInput = pinInput,
                                    onPinChange = { pinInput = it },
                                    appLockType = appLockType,
                                    viewModel = viewModel,
                                    onUnlockSuccess = onUnlockSuccess,
                                    onError = { errorMsg ->
                                        authMessage = errorMsg
                                        pinInput = ""
                                    },
                                    lockedAppUnlockMethod = lockedAppUnlockMethod,
                                    fromSplash = fromSplash,
                                    onSwitchToBiometric = { currentUiMode = activeUnlockMethod },
                                    showPasswordVisibility = showPasswordVisibility,
                                    onToggleVisibility = { showPasswordVisibility = !showPasswordVisibility }
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_exit),
                        contentDescription = "Exit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Exit",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BiometricAuthUI(
    biometricAuthenticator: BiometricAuthenticator,
    currentUiMode: String,
    lockedAppUnlockMethod: String,
    fromSplash: Boolean,
    onSwitchToPin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Scanning...",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline
        )

        LaunchedEffect(currentUiMode) {
            biometricAuthenticator.authenticate(
                allowDeviceCredential = currentUiMode == "device_lock"
            )
        }

        if (!fromSplash) {
            Spacer(modifier = Modifier.height(40.dp))

            if (lockedAppUnlockMethod == "biometric") {
                Button(
                    onClick = {
                        biometricAuthenticator.authenticate(
                            allowDeviceCredential = currentUiMode == "device_lock"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Try Again")
                }
            } else {
                TextButton(
                    onClick = onSwitchToPin,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Use PIN Instead")
                }
            }
        }
    }
}

@Composable
private fun PinAuthUI(
    pinInput: String,
    onPinChange: (String) -> Unit,
    appLockType: String,
    viewModel: UnlockViewModel,
    onUnlockSuccess: () -> Unit,
    onError: (String) -> Unit,
    lockedAppUnlockMethod: String,
    fromSplash: Boolean,
    onSwitchToBiometric: () -> Unit,
    showPasswordVisibility: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPassword = appLockType == "Password"
        val inputLabel = if (isPassword) "Enter Password" else "Enter PIN"

        OutlinedTextField(
            value = pinInput,
            onValueChange = onPinChange,
            label = { Text(inputLabel) },
            visualTransformation = if (showPasswordVisibility) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password
                else KeyboardType.NumberPassword
            ),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        painter = painterResource(
                            id = if (showPasswordVisibility) {
                                R.drawable.ic_visibility  // Your drawable for eye icon
                            } else {
                                R.drawable.ic_visibility_off  // Your drawable for eye with slash icon
                            }
                        ),
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (viewModel.verifyPassword(pinInput)) {
                    onUnlockSuccess()
                } else {
                    onError(if (isPassword) "Incorrect Password" else "Incorrect PIN")
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = pinInput.isNotEmpty(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Unlock", modifier = Modifier.padding(vertical = 8.dp))
        }

        if (!fromSplash && lockedAppUnlockMethod == "both") {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onSwitchToBiometric,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Use Biometric Instead")
            }
        }
    }
}