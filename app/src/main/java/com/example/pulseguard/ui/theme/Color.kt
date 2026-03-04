// com.example.pulseguard.ui.theme.Color
package com.example.pulseguard.ui.theme

import androidx.compose.ui.graphics.Color

// ── Pulse Guard Health Palette ────────────────────────────────────────────────
// Primary: Cardiac Red – evokes health, urgency awareness, cardiovascular domain.

/** Light-theme primary: deep cardiac red. */
val PulseRed40 = Color(0xFFC62828)

/** Dark-theme primary: soft rose for readability on dark backgrounds. */
val PulseRed80 = Color(0xFFFFB4AB)

/** Light-theme onPrimary. */
val PulseRed100 = Color(0xFFFFFFFF)

/** Dark-theme onPrimary. */
val PulseRed20 = Color(0xFF680003)

/** Light-theme primaryContainer. */
val PulseRedContainer = Color(0xFFFFDAD6)

/** Dark-theme primaryContainer. */
val PulseRedContainerDark = Color(0xFF93000A)

// Secondary: Medical Teal – calming, clinical, trustworthy.

/** Light-theme secondary. */
val MedTeal40 = Color(0xFF006874)

/** Dark-theme secondary. */
val MedTeal80 = Color(0xFF4FD8EB)

/** Light-theme onSecondary. */
val MedTeal100 = Color(0xFFFFFFFF)

/** Dark-theme onSecondary. */
val MedTeal20 = Color(0xFF00363D)

/** Light-theme secondaryContainer. */
val MedTealContainer = Color(0xFFAEEFF5)

/** Dark-theme secondaryContainer. */
val MedTealContainerDark = Color(0xFF004F57)

// Tertiary: Amber – used for "High Normal" warning tones.

/** Light-theme tertiary. */
val WarmAmber40 = Color(0xFF7B5800)

/** Dark-theme tertiary. */
val WarmAmber80 = Color(0xFFF9BC3F)

/** Light-theme tertiaryContainer. */
val WarmAmberContainer = Color(0xFFFFDEA6)

/** Dark-theme tertiaryContainer. */
val WarmAmberContainerDark = Color(0xFF594000)

// Surface neutrals
/** Light surface. */
val NeutralSurface = Color(0xFFFAFDFD)

/** Dark surface. */
val NeutralSurfaceDark = Color(0xFF191C1D)

// ── WHO/ESH Blood-Pressure Category Colours (implementation_plan.md §2.6) ────

/** Optimal: systolic < 120 and diastolic < 80. */
val CategoryOptimal = Color(0xFF4CAF50)

/** Normal: systolic 120–129 or diastolic 80–84. */
val CategoryNormal = Color(0xFF8BC34A)

/** High-Normal: systolic 130–139 or diastolic 85–89. */
val CategoryHighNormal = Color(0xFFFFC107)

/** Hypertension Grade 1: systolic 140–159 or diastolic 90–99. */
val CategoryHypertension1 = Color(0xFFFF9800)

/** Hypertension Grade 2: systolic 160–179 or diastolic 100–109. */
val CategoryHypertension2 = Color(0xFFF44336)

/** Hypertension Grade 3: systolic ≥ 180 or diastolic ≥ 110. */
val CategoryHypertension3 = Color(0xFFB71C1C)
