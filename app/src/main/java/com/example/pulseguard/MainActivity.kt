// com.example.pulseguard.MainActivity
package com.example.pulseguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pulseguard.ui.navigation.PulseGuardNavGraph
import com.example.pulseguard.ui.theme.PulseGuardTheme

/**
 * Single Activity entry point for Pulse Guard.
 *
 * Sets up edge-to-edge rendering and delegates all UI composition to
 * [PulseGuardNavGraph] wrapped in [PulseGuardTheme].
 * All navigation state is managed inside [PulseGuardNavGraph]; this Activity
 * is intentionally kept thin.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseGuardTheme {
                PulseGuardNavGraph()
            }
        }
    }
}
