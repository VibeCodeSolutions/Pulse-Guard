// com.example.pulseguard.data.local.entity.BloodPressureEntry
package com.example.pulseguard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pulseguard.domain.model.MeasurementArm

/**
 * Room entity representing a single blood pressure measurement entry.
 *
 * Stored in the `blood_pressure_entries` table. All numeric fields are
 * validated against allowed ranges before insertion (see Validation in
 * [com.example.pulseguard.domain.usecase.AddMeasurementUseCase]).
 *
 * @property id             Auto-generated primary key.
 * @property systolic       Systolic pressure in mmHg. Valid range: 60–300.
 * @property diastolic      Diastolic pressure in mmHg. Valid range: 30–200.
 * @property pulse          Heart rate in bpm. Valid range: 30–250.
 * @property measurementArm Which arm was used for the measurement.
 * @property medicationTaken Whether medication was taken before this measurement.
 * @property timestamp      Unix epoch milliseconds when the measurement was taken.
 * @property note           Optional free-text note attached to this measurement.
 */
@Entity(tableName = "blood_pressure_entries")
data class BloodPressureEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "systolic")
    val systolic: Int,

    @ColumnInfo(name = "diastolic")
    val diastolic: Int,

    @ColumnInfo(name = "pulse")
    val pulse: Int,

    @ColumnInfo(name = "measurement_arm")
    val measurementArm: MeasurementArm,

    @ColumnInfo(name = "medication_taken")
    val medicationTaken: Boolean,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "note")
    val note: String? = null,
)
