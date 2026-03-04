// com.example.pulseguard.MainActivity
package com.example.pulseguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.pulseguard.ui.navigation.PulseGuardNavGraph
import com.example.pulseguard.ui.theme.PulseGuardTheme

/**
 * Single Activity entry point for Pulse Guard.
 *
 * Installs the Android 12+ Splash Screen via [installSplashScreen] before
 * [super.onCreate] so the system can read the splash window attributes from
 * [Theme.PulseGuard]. After the splash exits, the Activity theme transitions
 * to [Theme.PulseGuard.App] via the `postSplashScreenTheme` attribute.
 *
 * Sets up edge-to-edge rendering and delegates all UI composition to
 * [PulseGuardNavGraph] wrapped in [PulseGuardTheme].
 * All navigation state is managed inside [PulseGuardNavGraph]; this Activity
 * is intentionally kept thin.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseGuardTheme {
                PulseGuardNavGraph()
            }
        }
    }
}
