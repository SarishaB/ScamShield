/*
 * ScamShield frontend redesign.
 * The original Circle-to-Search setup flow is preserved in the repository history;
 * this entry point now presents ScamShield as the primary product experience.
 */
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.akslabs.circletosearch.ui.OcrSettingsScreen
import com.akslabs.circletosearch.ui.OverlaySettingsScreen
import com.akslabs.circletosearch.ui.theme.CircleToSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CircleToSearchTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var screen by remember { mutableStateOf<ScamShieldScreen>(ScamShieldScreen.Home) }

                    Crossfade(targetState = screen, label = "scamshield-navigation") { target ->
                        when (target) {
                            ScamShieldScreen.Home -> ScamShieldHomeScreen(
                                onManualScan = { screen = ScamShieldScreen.ManualScan },
                                onCommunity = { screen = ScamShieldScreen.Community },
                                onSettings = { screen = ScamShieldScreen.Settings },
                                onOcrSettings = { screen = ScamShieldScreen.OcrSettings }
                            )
                            ScamShieldScreen.ManualScan -> ManualScanScreen(onBack = { screen = ScamShieldScreen.Home })
                            ScamShieldScreen.Community -> CommunityScreen(onBack = { screen = ScamShieldScreen.Home })
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
    Home, ManualScan, Community, Settings, OcrSettings
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
