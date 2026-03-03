// com.example.pulseguard.data.local.entity.MinMaxValues
package com.example.pulseguard.data.local.entity

/**
 * Holds minimum and maximum blood pressure values returned by a Room query.
 *
 * Used as the result type for [com.example.pulseguard.data.local.dao.BloodPressureDao.getMinMaxForDateRange].
 * Room maps the SQL column aliases to these fields.
 *
 * @property minSystolic  Minimum systolic pressure in mmHg for the queried period.
 * @property maxSystolic  Maximum systolic pressure in mmHg for the queried period.
 * @property minDiastolic Minimum diastolic pressure in mmHg for the queried period.
 * @property maxDiastolic Maximum diastolic pressure in mmHg for the queried period.
 */
data class MinMaxValues(
    val minSystolic: Int,
    val maxSystolic: Int,
    val minDiastolic: Int,
    val maxDiastolic: Int,
)
