// com.example.pulseguard.domain.model.BloodPressureCategoryTest
package com.example.pulseguard.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [BloodPressureCategory.fromValues].
 *
 * Covers all six WHO categories, boundary values, and the "higher severity wins" rule
 * (when systolic and diastolic would map to different categories, the more severe wins).
 */
class BloodPressureCategoryTest {

    // ── Baseline cases ──────────────────────────────────────────────────────────

    @Test
    fun fromValues_bothClearlyOptimal_returnsOptimal() {
        assertEquals(BloodPressureCategory.OPTIMAL, BloodPressureCategory.fromValues(110, 70))
    }

    @Test
    fun fromValues_systolicNormalDiastolicOptimal_returnsNormal() {
        assertEquals(BloodPressureCategory.NORMAL, BloodPressureCategory.fromValues(125, 75))
    }

    @Test
    fun fromValues_systolicHighNormal_returnsHighNormal() {
        assertEquals(BloodPressureCategory.HIGH_NORMAL, BloodPressureCategory.fromValues(135, 75))
    }

    @Test
    fun fromValues_systolicHypertension1_returnsHypertension1() {
        assertEquals(BloodPressureCategory.HYPERTENSION_1, BloodPressureCategory.fromValues(150, 75))
    }

    @Test
    fun fromValues_systolicHypertension2_returnsHypertension2() {
        assertEquals(BloodPressureCategory.HYPERTENSION_2, BloodPressureCategory.fromValues(170, 75))
    }

    @Test
    fun fromValues_systolicHypertension3_returnsHypertension3() {
        assertEquals(BloodPressureCategory.HYPERTENSION_3, BloodPressureCategory.fromValues(185, 75))
    }

    // ── "Higher severity wins" rule ──────────────────────────────────────────────

    @Test
    fun fromValues_systolicOptimalDiastolicNormal_returnsNormal() {
        assertEquals(BloodPressureCategory.NORMAL, BloodPressureCategory.fromValues(115, 82))
    }

    @Test
    fun fromValues_diastolicHypertension1TakesPrecedenceOverOptimalSystolic_returnsHypertension1() {
        assertEquals(BloodPressureCategory.HYPERTENSION_1, BloodPressureCategory.fromValues(115, 95))
    }

    // ── Boundary values ──────────────────────────────────────────────────────────

    @Test
    fun fromValues_systolicExactly120_returnsNormal() {
        assertEquals(BloodPressureCategory.NORMAL, BloodPressureCategory.fromValues(120, 70))
    }

    @Test
    fun fromValues_systolicExactly119_returnsOptimal() {
        assertEquals(BloodPressureCategory.OPTIMAL, BloodPressureCategory.fromValues(119, 70))
    }

    @Test
    fun fromValues_diastolicExactly80_returnsNormal() {
        assertEquals(BloodPressureCategory.NORMAL, BloodPressureCategory.fromValues(110, 80))
    }

    @Test
    fun fromValues_diastolicExactly79_returnsOptimal() {
        assertEquals(BloodPressureCategory.OPTIMAL, BloodPressureCategory.fromValues(110, 79))
    }

    @Test
    fun fromValues_systolicExactly180_returnsHypertension3() {
        assertEquals(BloodPressureCategory.HYPERTENSION_3, BloodPressureCategory.fromValues(180, 70))
    }

    @Test
    fun fromValues_diastolicExactly110_returnsHypertension3() {
        assertEquals(BloodPressureCategory.HYPERTENSION_3, BloodPressureCategory.fromValues(110, 110))
    }

    // ── Extreme / validation-boundary values ─────────────────────────────────────

    @Test
    fun fromValues_minAllowedValues_returnsOptimal() {
        // systolic=60, diastolic=30 → both clearly optimal
        assertEquals(BloodPressureCategory.OPTIMAL, BloodPressureCategory.fromValues(60, 30))
    }

    @Test
    fun fromValues_maxAllowedValues_returnsHypertension3() {
        // systolic=300, diastolic=200
        assertEquals(BloodPressureCategory.HYPERTENSION_3, BloodPressureCategory.fromValues(300, 200))
    }
}
