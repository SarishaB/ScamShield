package com.akslabs.circletosearch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
                    var screen by remember { mutableStateOf<ScamShieldScreen>(if (initiallyEnabled) ScamShieldScreen.Home else ScamShieldScreen.Onboarding) }

                    BackHandler(enabled = screen != ScamShieldScreen.Home && screen != ScamShieldScreen.Onboarding) {
                        screen = when (screen) {
                            ScamShieldScreen.Report -> ScamShieldScreen.Community
                            ScamShieldScreen.OcrSettings -> ScamShieldScreen.Settings
                            ScamShieldScreen.Community, ScamShieldScreen.ManualScan, ScamShieldScreen.Settings -> ScamShieldScreen.Home
                            else -> ScamShieldScreen.Home
                        }
                    }

                    DisposableEffect(Unit) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                accessibilityEnabled = isAccessibilityServiceEnabled(this@MainActivity)
                                if (!accessibilityEnabled) screen = ScamShieldScreen.Onboarding
                                else if (screen == ScamShieldScreen.Onboarding) screen = ScamShieldScreen.Home
                            }
                        }
                        lifecycle.addObserver(observer)
                        onDispose { lifecycle.removeObserver(observer) }
                    }

                    Crossfade(targetState = screen, label = "scamshield-navigation") { target ->
                        when (target) {
                            ScamShieldScreen.Onboarding -> ScamShieldOnboardingScreen { openAccessibilitySettings(this@MainActivity) }
                            ScamShieldScreen.Home -> ScamShieldHomeScreenV2(
                                onScan = { screen = ScamShieldScreen.ManualScan },
                                onCommunity = { screen = ScamShieldScreen.Community },
                                onSettings = { screen = ScamShieldScreen.Settings }
                            )
                            ScamShieldScreen.ManualScan -> UnifiedScanScreen { screen = ScamShieldScreen.Home }
                            ScamShieldScreen.Community -> CommunityScreenV2(
                                onBack = { screen = ScamShieldScreen.Home },
                                onReport = { screen = ScamShieldScreen.Report }
                            )
                            ScamShieldScreen.Report -> ReportScamScreenV2 { screen = ScamShieldScreen.Community }
                            ScamShieldScreen.Settings -> OverlaySettingsScreen { screen = ScamShieldScreen.Home }
                            ScamShieldScreen.OcrSettings -> OcrSettingsScreen { screen = ScamShieldScreen.Settings }
                        }
                    }
                }
            }
        }
    }
}

private enum class ScamShieldScreen { Onboarding, Home, ManualScan, Community, Report, Settings, OcrSettings }

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expected = ComponentName(context, CircleToSearchAccessibilityService::class.java)
    val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val splitter = android.text.TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabled)
    while (splitter.hasNext()) if (ComponentName.unflattenFromString(splitter.next()) == expected) return true
    return false
}

fun isDefaultAssistant(context: Context): Boolean {
    val assistant = Settings.Secure.getString(context.contentResolver, "voice_interaction_service")
    return assistant == ComponentName(context, CircleToSearchVoiceService::class.java).flattenToString()
}

fun openAccessibilitySettings(context: Context) {
    try { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    catch (_: Exception) { Toast.makeText(context, "Could not open Accessibility Settings", Toast.LENGTH_LONG).show() }
}
