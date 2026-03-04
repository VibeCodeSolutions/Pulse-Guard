// com.example.pulseguard.domain.model.BloodPressureCategory
package com.example.pulseguard.domain.model

/**
 * WHO/ESH blood pressure classification categories.
 *
 * Thresholds follow the ESH 2023 hypertension guidelines.
 * UI colour mapping lives in the presentation layer (`BloodPressureCategory.toColor()`).
 */
enum class BloodPressureCategory {
    /** Optimal: systolic < 120 AND diastolic < 80. */
    OPTIMAL,

    /** Normal: systolic 120–129 OR diastolic 80–84. */
    NORMAL,

    /** High-Normal: systolic 130–139 OR diastolic 85–89. */
    HIGH_NORMAL,

    /** Hypertension Grade 1: systolic 140–159 OR diastolic 90–99. */
    HYPERTENSION_1,

    /** Hypertension Grade 2: systolic 160–179 OR diastolic 100–109. */
    HYPERTENSION_2,

    /** Hypertension Grade 3: systolic ≥ 180 OR diastolic ≥ 110. */
    HYPERTENSION_3;

    companion object {
        /**
         * Determines the blood pressure category from systolic and diastolic values.
         *
         * The more severe classification of the two pressures takes precedence
         * (WHO "higher value wins" rule). Enum ordinal order maps directly to
         * increasing severity, so `maxOf` is safe here.
         *
         * @param systolic  Systolic pressure in mmHg.
         * @param diastolic Diastolic pressure in mmHg.
         * @return The corresponding [BloodPressureCategory].
         */
        fun fromValues(systolic: Int, diastolic: Int): BloodPressureCategory {
            val bySystolic = when {
                systolic < 120 -> OPTIMAL
                systolic < 130 -> NORMAL
                systolic < 140 -> HIGH_NORMAL
                systolic < 160 -> HYPERTENSION_1
                systolic < 180 -> HYPERTENSION_2
                else -> HYPERTENSION_3
            }
            val byDiastolic = when {
                diastolic < 80 -> OPTIMAL
                diastolic < 85 -> NORMAL
                diastolic < 90 -> HIGH_NORMAL
                diastolic < 100 -> HYPERTENSION_1
                diastolic < 110 -> HYPERTENSION_2
                else -> HYPERTENSION_3
            }
            return maxOf(bySystolic, byDiastolic)
        }
    }
}
