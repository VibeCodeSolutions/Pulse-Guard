// com.example.pulseguard.data.local.Converters
package com.example.pulseguard.data.local

import androidx.room.TypeConverter
import com.example.pulseguard.domain.model.MeasurementArm
import com.example.pulseguard.domain.model.ReminderType

/**
 * Room TypeConverters for custom types that cannot be stored natively in SQLite.
 *
 * Registered at the database level via `@TypeConverters(Converters::class)` in
 * [PulseGuardDatabase], making it available for all entities and queries.
 */
class Converters {

    // ── MeasurementArm ─────────────────────────────────────────────────────

    /** Converts a [MeasurementArm] enum value to its [String] name for SQLite storage. */
    @TypeConverter
    fun fromMeasurementArm(arm: MeasurementArm): String = arm.name

    /** Converts a [String] from the database back to the corresponding [MeasurementArm]. */
    @TypeConverter
    fun toMeasurementArm(value: String): MeasurementArm = MeasurementArm.valueOf(value)

    // ── ReminderType ───────────────────────────────────────────────────────

    /** Converts a [ReminderType] enum value to its [String] name for SQLite storage. */
    @TypeConverter
    fun fromReminderType(type: ReminderType): String = type.name

    /** Converts a [String] from the database back to the corresponding [ReminderType]. */
    @TypeConverter
    fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)
}
