package com.akslabs.circletosearch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.akslabs.circletosearch.ui.OcrSettingsScreen
import com.akslabs.circletosearch.ui.OverlaySettingsScreen
import com.akslabs.circletosearch.ui.theme.CircleToSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircleToSearchTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val initiallyEnabled = isAccessibilityServiceEnabled(this@MainActivity)
                    var accessibilityEnabled by remember { mutableStateOf(initiallyEnabled) }
                    var screen by remember {
                        mutableStateOf<ScamShieldScreen>(
                            if (initiallyEnabled) ScamShieldScreen.Home else ScamShieldScreen.Onboarding
                        )
                    }

                    DisposableEffect(Unit) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                accessibilityEnabled = isAccessibilityServiceEnabled(this@MainActivity)
                                screen = if (accessibilityEnabled) {
                                    if (screen == ScamShieldScreen.Onboarding) ScamShieldScreen.Home else screen
                                } else {
                                    ScamShieldScreen.Onboarding
                                }
                            }
                        }
                        lifecycle.addObserver(observer)
                        onDispose { lifecycle.removeObserver(observer) }
                    }

                    Crossfade(targetState = screen, label = "scamshield-navigation") { target ->
                        when (target) {
                            ScamShieldScreen.Onboarding -> ScamShieldOnboardingScreen(
                                onEnable = { openAccessibilitySettings(this@MainActivity) }
                            )
                            ScamShieldScreen.Home -> ScamShieldHomeScreen(
                                onManualScan = { screen = ScamShieldScreen.ManualScan },
                                onScreenshot = { screen = ScamShieldScreen.ScreenshotInput },
                                onCommunity = { screen = ScamShieldScreen.Community },
                                onSettings = { screen = ScamShieldScreen.Settings }
                            )
                            ScamShieldScreen.ManualScan -> ManualScanScreen(onBack = { screen = ScamShieldScreen.Home })
                            ScamShieldScreen.ScreenshotInput -> ScreenshotInputScreen(onBack = { screen = ScamShieldScreen.Home })
                            ScamShieldScreen.Community -> CommunityScreen(
                                onBack = { screen = ScamShieldScreen.Home },
                                onReport = { screen = ScamShieldScreen.Report }
                            )
                            ScamShieldScreen.Report -> ReportScamScreen(onBack = { screen = ScamShieldScreen.Community })
                            ScamShieldScreen.Settings -> OverlaySettingsScreen(onBack = { screen = ScamShieldScreen.Home })
                            ScamShieldScreen.OcrSettings -> OcrSettingsScreen(onBack = { screen = ScamShieldScreen.Settings })
                        }
                    }
                }
            }
        }
    }
}

private enum class ScamShieldScreen {
    Onboarding, Home, ManualScan, ScreenshotInput, Community, Report, Settings, OcrSettings
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expected = ComponentName(context, CircleToSearchAccessibilityService::class.java)
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val splitter = android.text.TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabled)
    while (splitter.hasNext()) {
        val component = ComponentName.unflattenFromString(splitter.next())
        if (component == expected) return true
    }
    return false
}

fun isDefaultAssistant(context: Context): Boolean {
    val assistant = Settings.Secure.getString(context.contentResolver, "voice_interaction_service")
    return assistant == ComponentName(context, CircleToSearchVoiceService::class.java).flattenToString()
}

fun openAccessibilitySettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open Accessibility Settings", Toast.LENGTH_LONG).show()
    }
}
