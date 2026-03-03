// com.example.pulseguard.data.local.PulseGuardDatabase
package com.example.pulseguard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pulseguard.data.local.dao.BloodPressureDao
import com.example.pulseguard.data.local.entity.BloodPressureEntry

/**
 * Central Room database for Pulse Guard.
 *
 * **Version history**
 * - `v1`: Initial schema — `blood_pressure_entries` table.
 *
 * `exportSchema = true` writes a JSON schema file to `app/schemas/` on every build,
 * enabling diff-based migration tracking (see `ksp { arg("room.schemaLocation", …) }`
 * in `app/build.gradle.kts`). From version 2 onwards, explicit [androidx.room.migration.Migration]
 * objects must be provided.
 */
@Database(
    entities = [BloodPressureEntry::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class PulseGuardDatabase : RoomDatabase() {

    /** Returns the DAO for blood pressure measurement entries. */
    abstract fun bloodPressureDao(): BloodPressureDao
}
