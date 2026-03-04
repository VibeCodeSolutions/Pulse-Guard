// com.example.pulseguard.ui.theme.CategoryColorExt
package com.example.pulseguard.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.pulseguard.domain.model.BloodPressureCategory

/**
 * Maps a [BloodPressureCategory] to its corresponding [Color] from the app's theme palette.
 *
 * Keeping this mapping in the UI layer preserves Clean Architecture: the domain model
 * stays free of presentation details, and theming (e.g. dark-mode overrides) can be
 * introduced here without touching business logic.
 */
fun BloodPressureCategory.toColor(): Color = when (this) {
    BloodPressureCategory.OPTIMAL -> CategoryOptimal
    BloodPressureCategory.NORMAL -> CategoryNormal
    BloodPressureCategory.HIGH_NORMAL -> CategoryHighNormal
    BloodPressureCategory.HYPERTENSION_1 -> CategoryHypertension1
    BloodPressureCategory.HYPERTENSION_2 -> CategoryHypertension2
    BloodPressureCategory.HYPERTENSION_3 -> CategoryHypertension3
}
