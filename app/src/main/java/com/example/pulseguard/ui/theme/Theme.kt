// com.example.pulseguard.ui.theme.Theme
package com.example.pulseguard.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PulseRed80,
    onPrimary = PulseRed20,
    primaryContainer = PulseRedContainerDark,
    secondary = MedTeal80,
    onSecondary = MedTeal20,
    secondaryContainer = MedTealContainerDark,
    tertiary = WarmAmber80,
    tertiaryContainer = WarmAmberContainerDark,
    surface = NeutralSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PulseRed40,
    onPrimary = PulseRed100,
    primaryContainer = PulseRedContainer,
    secondary = MedTeal40,
    onSecondary = MedTeal100,
    secondaryContainer = MedTealContainer,
    tertiary = WarmAmber40,
    tertiaryContainer = WarmAmberContainer,
    surface = NeutralSurface,
)

/**
 * Root Compose theme for Pulse Guard.
 *
 * Applies Material3 Dynamic Colors (Material You) on Android 12+ (API 31+)
 * with a static fallback palette built from the Pulse Guard health colour system
 * for older devices and/or Compose Previews.
 *
 * The static palette uses:
 * - **Primary** – Cardiac Red ([PulseRed40] / [PulseRed80]): evokes the cardiovascular domain.
 * - **Secondary** – Medical Teal ([MedTeal40] / [MedTeal80]): calming, clinical, trustworthy.
 * - **Tertiary** – Warm Amber ([WarmAmber40] / [WarmAmber80]): used for high-normal warnings.
 *
 * @param darkTheme     Whether to use the dark colour scheme. Defaults to system setting.
 * @param dynamicColor  Whether to apply Material You dynamic colours. Defaults to true.
 * @param content       The composable content to render within this theme.
 */
@Composable
fun PulseGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
