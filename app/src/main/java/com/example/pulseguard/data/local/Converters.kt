// com.example.pulseguard.data.local.Converters
package com.example.pulseguard.data.local

import androidx.room.TypeConverter
import com.example.pulseguard.domain.model.MeasurementArm

/**
 * Room TypeConverters for custom types that cannot be stored natively in SQLite.
 *
 * Registered at the database level via `@TypeConverters(Converters::class)` in
 * [PulseGuardDatabase], making it available for all entities and queries.
 */
class Converters {

    /**
     * Converts a [MeasurementArm] enum value to its [String] name for SQLite storage.
     *
     * @param arm The [MeasurementArm] value to convert.
     * @return The string representation of the enum (e.g. `"LEFT"` or `"RIGHT"`).
     */
    @TypeConverter
    fun fromMeasurementArm(arm: MeasurementArm): String = arm.name

    /**
     * Converts a [String] from the database back to the corresponding [MeasurementArm] value.
     *
     * @param value The string name of the enum value stored in the database.
     * @return The corresponding [MeasurementArm] enum constant.
     */
    @TypeConverter
    fun toMeasurementArm(value: String): MeasurementArm = MeasurementArm.valueOf(value)
}
