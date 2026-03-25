// com.example.pulseguard.domain.model.BloodPressureCategory
package com.example.pulseguard.domain.model

/**
 * Blood pressure classification categories per ESC/ESH 2023 guidelines.
 *
 * ### ESC/ESH 2023 (implemented)
 * | Category       | Systolic (mmHg) | Diastolic (mmHg) |
 * |----------------|-----------------|------------------|
 * | Optimal        | < 120           | AND < 80         |
 * | Normal         | 120–129         | OR 80–84         |
 * | High-Normal    | 130–139         | OR 85–89         |
 * | Hypertension 1 | 140–159         | OR 90–99         |
 * | Hypertension 2 | 160–179         | OR 100–109       |
 * | Hypertension 3 | >= 180          | OR >= 110        |
 *
 * ### AHA/ACC 2017 (American — reference only, not implemented)
 * | Category             | Systolic (mmHg) | Diastolic (mmHg) |
 * |----------------------|-----------------|------------------|
 * | Normal               | < 120           | AND < 80         |
 * | Elevated             | 120–129         | AND < 80         |
 * | Hypertension Stage 1 | 130–139         | OR 80–89         |
 * | Hypertension Stage 2 | >= 140          | OR >= 90         |
 * | Hypertensive Crisis  | > 180           | AND/OR > 120     |
 *
 * Key differences: AHA classifies 130–139 / 80–89 as Stage 1 Hypertension
 * (ESC/ESH: High-Normal). AHA has no "High-Normal" category and uses coarser
 * granularity at higher ranges (single Stage 2 vs. ESC/ESH Grade 2 + 3).
 *
 * UI colour mapping lives in the presentation layer
 * (`BloodPressureCategory.toColor()`).
 *
 * @see <a href="https://doi.org/10.1093/eurheartj/ehad192">
 *   2023 ESH Guidelines (European Heart Journal)</a>
 * @see <a href="https://doi.org/10.1161/HYP.0000000000000065">
 *   2017 ACC/AHA Guideline (Hypertension)</a>
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
         * Determines the blood pressure category from systolic and diastolic
         * values per ESC/ESH 2023 classification.
         *
         * The more severe classification of the two pressures takes precedence
         * (ESC/ESH "higher category wins" rule). Enum ordinal order maps
         * directly to increasing severity, so [maxOf] is safe here.
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
