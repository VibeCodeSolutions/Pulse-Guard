// com.example.pulseguard.domain.model.DashboardAggregation
package com.example.pulseguard.domain.model

/**
 * Aggregated blood pressure data for a specific time period, consumed by the Dashboard screen.
 *
 * Built by [com.example.pulseguard.domain.usecase.GetDashboardDataUseCase] from raw
 * DAO query results.
 *
 * @property periodLabel  Human-readable label for the period (e.g. "March 2026", "KW 10").
 * @property avgSystolic  Average systolic pressure in mmHg.
 * @property avgDiastolic Average diastolic pressure in mmHg.
 * @property avgPulse     Average pulse rate in bpm.
 * @property minSystolic  Minimum systolic pressure recorded in the period.
 * @property maxSystolic  Maximum systolic pressure recorded in the period.
 * @property minDiastolic Minimum diastolic pressure recorded in the period.
 * @property maxDiastolic Maximum diastolic pressure recorded in the period.
 * @property entryCount   Number of measurements in the period.
 * @property category     Derived [BloodPressureCategory] based on the average values.
 */
data class DashboardAggregation(
    val periodLabel: String,
    val avgSystolic: Float,
    val avgDiastolic: Float,
    val avgPulse: Float,
    val minSystolic: Int,
    val maxSystolic: Int,
    val minDiastolic: Int,
    val maxDiastolic: Int,
    val entryCount: Int,
    val category: BloodPressureCategory,
)
