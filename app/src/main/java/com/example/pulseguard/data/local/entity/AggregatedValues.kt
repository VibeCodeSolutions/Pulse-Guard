// com.example.pulseguard.data.local.entity.AggregatedValues
package com.example.pulseguard.data.local.entity

/**
 * Holds aggregated (average) blood pressure and pulse values returned by a Room query.
 *
 * Used as the result type for [com.example.pulseguard.data.local.dao.BloodPressureDao.getAverageForDateRange].
 * Room maps the SQL column aliases `avgSystolic`, `avgDiastolic`, and `avgPulse` to these fields.
 *
 * @property avgSystolic  Average systolic pressure in mmHg.
 * @property avgDiastolic Average diastolic pressure in mmHg.
 * @property avgPulse     Average pulse rate in bpm.
 */
data class AggregatedValues(
    val avgSystolic: Float,
    val avgDiastolic: Float,
    val avgPulse: Float,
)
