// com.example.pulseguard.domain.model.ChartDataPoint
package com.example.pulseguard.domain.model

/**
 * A single data point for the blood pressure trend chart.
 *
 * @property x         Sequential index (0 = oldest entry in the period, chronologically sorted).
 * @property systolic  Systolic pressure in mmHg.
 * @property diastolic Diastolic pressure in mmHg.
 * @property timestamp Unix epoch milliseconds of the original measurement.
 */
data class ChartDataPoint(
    val x: Float,
    val systolic: Float,
    val diastolic: Float,
    val timestamp: Long,
)
